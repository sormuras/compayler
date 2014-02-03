package de.sormuras.compayler.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Field;
import de.sormuras.compayler.model.Kind;
import de.sormuras.compayler.model.Mode;
import de.sormuras.compayler.model.Type;
import de.sormuras.compayler.model.Unit;
import de.sormuras.compayler.service.SourceFactory;

public class DefaultSourceFactory implements SourceFactory {

  public static String merge(String head, String tail, String separator, Object... objects) {
    StringBuilder builder = new StringBuilder();
    int count = 0;
    builder.append(head);
    for (Object var : objects) {
      if (count > 0)
        builder.append(separator);
      builder.append(var);
      count++;
    }
    builder.append(tail);
    return count > 0 ? builder.toString() : "";
  }

  public static String now() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    return df.format(new Date());
  }

  protected Compayler compayler;
  protected Lines lines;

  protected void addClassComment() {
    lines.add("");
    lines.add("/**");
    lines.add(" * Class %s generated for %s.", compayler.getDecoratorClassName(), compayler.getInterfaceName());
    lines.add(" *");
    lines.add(" * Generated on %s.", now());
    lines.add(" */");
  }

  protected void addDecoratorClass(List<Unit<?>> units) {
    Type closeableType = new Type("java.io.Closeable");
    Type interfaceType = new Type(compayler.getInterfaceClassName(), ""); // TODO Care about type arguments.
    StringBuilder line = new StringBuilder();
    line.append("public class ").append(compayler.getDecoratorName());
    line.append(" extends ").append(compayler.getSuperClassName());
    line.append(" implements ").append(merge("", "", ", ", closeableType, interfaceType));
    line.append(" {");
    lines.add(line.toString());
    lines.pushIndention();

    lines.add("");
    lines.add("private interface Executable {");
    lines.pushIndention();
    for (Unit<?> unit : units) {
      addExecutableClass(unit);
    }
    lines.popIndention();
    lines.add("");
    lines.add("}"); // end of interface

    addDecoratorClassFieldsAndConstructor();
    addDecoratorClassObjectMethods();
    addDecoratorClassUtilities();
    for (Unit<?> unit : units) {
      addDecoratorMethod(unit);
    }
    // TODO add implementations using Executable classes from above

    lines.popIndention();
    lines.add("");
    lines.add("}"); // end of decorator class
  }

  protected void addDecoratorClassFieldsAndConstructor() {
    String typeVariables = ""; // TODO configuration.getTypeParameterParenthesis();
    String interfaceName = compayler.getInterfaceClassName().replace('$', '.') + typeVariables;
    lines.add("");
    lines.add("protected final org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler;");
    lines.add("protected final " + interfaceName + " prevalentSystem;");
    lines.add("");
    lines.add("public " + compayler.getDecoratorName() + "(org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler) {");
    lines.pushIndention();
    lines.add("this.prevayler = prevayler;");
    lines.add("this.prevalentSystem = prevayler.prevalentSystem();");
    lines.popIndention();
    lines.add("}");
  }

  // hashCode(), equals() and toString() methods declared in java.lang.Object
  protected void addDecoratorClassObjectMethods() {
    lines.add("");
    lines.add("@Override");
    lines.add("public boolean equals(Object obj) {");
    lines.pushIndention().add("return prevalentSystem.equals(obj);").popIndention();
    lines.add("}");
    lines.add("");
    lines.add("@Override");
    lines.add("public int hashCode() {");
    lines.pushIndention().add("return prevalentSystem.hashCode();").popIndention();
    lines.add("}");
    lines.add("");
    lines.add("@Override");
    lines.add("public String toString() {");
    lines.pushIndention().add("return prevalentSystem.toString();").popIndention();
    lines.add("}");

  }

  protected void addDecoratorClassUtilities() {
    String typeVariables = ""; // TODO configuration.getTypeParameterParenthesis();
    String interfaceName = compayler.getInterfaceClassName().replace('$', '.') + typeVariables;

    lines.add("");
    lines.add("@Override");
    lines.add("public void close() throws java.io.IOException {");
    lines.pushIndention().add("prevayler.close();").popIndention();
    lines.add("}");
    lines.add("");
    lines.add("public org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler() {");
    lines.pushIndention().add("return prevayler;").popIndention();
    lines.add("}");
    // lines.add("");
    // lines.add("protected " + interfaceName + " redirect(" + interfaceName + " result) {");
    // lines.pushIndention().add("return result == prevalentSystem ? this : result;").popIndention();
    // lines.add("}");
  }

  protected void addDecoratorMethod(Unit<?> unit) {
    String name = unit.getSignature().getName();
    List<Field> fields = unit.getSignature().getFields();
    if (name.equals("equals") && fields.size() == 1 && fields.get(0).getType().getBinaryName().equals("java.lang.Object"))
      return;
    if (name.equals("hashCode") && fields.isEmpty())
      return;
    if (name.equals("toString") && fields.isEmpty())
      return;

    Kind kind = unit.generateKind();
    Type returns = unit.getSignature().getReturnType();

    // method head
    lines.add("");
    lines.add("/**");
    lines.add(" * " + unit.generateMethodDeclaration());
    lines.add(" */ ");
    lines.add("@Override");
    lines.add("public " + unit.generateMethodDeclaration() + " {");
    lines.pushIndention();

    // direct?
    if (unit.getMode() == Mode.DIRECT) {
      String invokeMethodDirect = "prevalentSystem." + name + unit.generateParameterParentheses();
      if (returns.isVoid())
        lines.add("%s;", invokeMethodDirect);
      else {
        lines.add("%s result = %s;", returns, invokeMethodDirect);
        lines.add("return result == prevalentSystem ? this : result;");
      }
      lines.popIndention();
      lines.add("}"); // end of method
      return;
    }

    // // instantiate executable/action and let prevayler do the work
    String fullClassName = "Executable." + unit.generateClassNameWithTypeVariables();
    String newAction = "new " + fullClassName + unit.generateParameterParentheses();
    String assignAction = fullClassName + " action = " + newAction;
    String executeAction = "prevayler.execute(action)";
    lines.add(assignAction + ";");

    if (kind.isThrowingException()) {
      lines.add("try {");
      lines.pushIndention();
    }

    if (returns.isVoid()) {
      lines.add(executeAction + ";");
    } else {
      lines.add(returns + " result = " + executeAction + ";");
      lines.add("return result == prevalentSystem ? this : result;");
    }

    if (kind.isThrowingException()) {
      lines.popIndention();
      if (!unit.getSignature().getThrowables().isEmpty()) {
        lines.add("} catch (" + unit.generateExceptions("|") + " e) {");
        lines.pushIndention().add("throw e;").popIndention();
      }
      if (!unit.getSignature().getThrowables().contains(Exception.class)) { // FIXME scan for binary name or use type instance
        lines.add("} catch (java.lang.Exception e) {");
        lines.pushIndention().add("throw new RuntimeException(e);").popIndention();
      }

      lines.add("}");
    }

    lines.popIndention();
    lines.add("}"); // end of method
  }

  protected void addExecutableClass(Unit<?> unit) {
    lines.add("");
    lines.add("class %s implements %s {", unit.generateClassNameWithTypeVariables(), unit.generateImplements());
    lines.pushIndention();
    lines.add("");
    lines.add("private static final long serialVersionUID = %dL;", unit.getSerialVersionUID());
    addExecutableClassFieldsAndConstructor(unit);
    addExecutableImplementation(unit);
    lines.popIndention();
    lines.add("");
    lines.add("}");
  }

  protected void addExecutableClassFieldsAndConstructor(Unit<?> unit) {
    if (unit.getSignature().getFields().isEmpty())
      return;
    lines.add("");
    for (Field field : unit.getSignature().getFields()) {
      if (field.isTime())
        lines.add("@SuppressWarnings(\"unused\")");
      lines.add("private final %s %s;", field.getType().toString(false), field.getName());
    }
    lines.add("");
    lines.add("public %s%s {", unit.generateClassName(), unit.generateParameterSignature());
    lines.pushIndention();
    for (Field field : unit.getSignature().getFields()) {
      lines.add("this.%s = %1$s;", field.getName());
    }
    lines.popIndention();
    lines.add("}");
  }

  protected void addExecutableImplementation(Unit<?> unit) {
    // implementation
    String typeVariables = ""; // TODO configuration.getTypeParameterParenthesis();
    String parameters = compayler.getInterfaceClassName().replace('$', '.') + typeVariables
        + " prevalentSystem, java.util.Date executionTime";
    Type returnType = unit.getSignature().getReturnType();
    String methodCall = unit.getSignature().getName() + unit.generateParameterParenthesesWithExecutionTime();
    lines.add("");
    lines.add("@Override");
    switch (unit.generateKind()) {
    case QUERY:
      lines.add("public %s query(%s) throws java.lang.Exception {", returnType, parameters);
      lines.pushIndention().add("return prevalentSystem.%s;", methodCall).popIndention();
      break;
    case TRANSACTION:
      lines.add("public void executeOn(%s) {", parameters);
      lines.pushIndention().add("prevalentSystem." + methodCall + ";").popIndention();
      break;
    case TRANSACTION_QUERY:
      lines.add("public %s executeAndQuery(%s) {", returnType, parameters);
      lines.pushIndention().add("return prevalentSystem.%s;", methodCall).popIndention();
      break;
    case TRANSACTION_QUERY_EXCEPTION:
      lines.add("public %s executeAndQuery(%s) throws java.lang.Exception {", returnType, parameters);
      lines.pushIndention().add("return prevalentSystem.%s;", methodCall).popIndention();
      break;
    }
    lines.add("}"); // end of implementation
  }

  protected void addPackage() {
    String name = compayler.getDecoratorPackage();
    if (name == null || name.isEmpty())
      return;
    // no empty line before package declaration
    lines.add("package %s;", name);
  }

  @Override
  public Source createSource(Compayler compayler, List<Unit<?>> units) {
    this.compayler = compayler;
    this.lines = new Lines();
    addPackage();
    addClassComment();
    addDecoratorClass(units);
    return new Source(compayler.getDecoratorPackage(), compayler.getDecoratorName(), lines.getLines());
  }

}
