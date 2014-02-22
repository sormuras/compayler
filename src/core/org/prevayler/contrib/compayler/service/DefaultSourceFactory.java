package org.prevayler.contrib.compayler.service;

import static org.prevayler.contrib.compayler.Util.merge;

import java.util.List;
import java.util.Random;

import org.prevayler.contrib.compayler.Generator;
import org.prevayler.contrib.compayler.Mode;
import org.prevayler.contrib.compayler.Param;
import org.prevayler.contrib.compayler.Repository;
import org.prevayler.contrib.compayler.Source;
import org.prevayler.contrib.compayler.SourceFactory;
import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Unit;

public class DefaultSourceFactory extends Listing implements SourceFactory {

  protected final Generator generator = new DefaultGenerator();
  protected final Random random = new Random();
  protected Repository repository;

  protected void addClassComment() {
    if (!list().isEmpty())
      add("");
    add("/**");
    add(" * Class %s generated for %s.", repository.getDecoratorType(), repository.getInterfaceType());
    add(" *");
    add(" * Generated on %s.", generator.now());
    add(" */");
  }

  protected void addDecoratorClass() {
    Type closeableType = new Type("java.io.Closeable");
    Type interfaceType = repository.getInterfaceType();
    StringBuilder line = new StringBuilder();
    line.append("public class ").append(repository.getDecoratorType().getSimpleName());
    line.append(" extends ").append(repository.getSuperType());
    line.append(" implements ").append(merge("", "", ", ", closeableType, interfaceType));
    line.append(" {");
    add(line.toString());
    inc();

    add("");
    add("public interface Executable {");
    inc();
    for (Unit unit : repository.getUnits()) {
      addExecutableClass(unit);
    }
    dec();
    add("");
    add("}"); // end of interface

    addDecoratorClassFieldsAndConstructor();
    addDecoratorClassObjectMethods();
    addDecoratorClassUtilities();
    for (Unit unit : repository.getUnits()) {
      addDecoratorMethod(unit);
    }

    dec();
    add("");
    add("}"); // end of decorator class
  }

  protected void addDecoratorClassFieldsAndConstructor() {
    String interfaceName = repository.getInterfaceType().getCanonicalName();
    add("");
    add("protected final org.prevayler.Prevayler<? extends %s> prevayler;", interfaceName);
    add("protected final %s prevalentSystem;", interfaceName);
    add("");
    add("public %s(org.prevayler.Prevayler<? extends %s> prevayler) {", repository.getDecoratorType().getSimpleName(), interfaceName);
    inc();
    add("this.prevayler = prevayler;");
    add("this.prevalentSystem = prevayler.prevalentSystem();");
    dec();
    add("}");
  }

  // hashCode(), equals() and toString() methods declared in java.lang.Object
  protected void addDecoratorClassObjectMethods() {
    add("");
    add("@Override");
    add("public boolean equals(Object obj) {");
    inc().add("return prevalentSystem.equals(obj);").dec();
    add("}");
    add("");
    add("@Override");
    add("public int hashCode() {");
    inc().add("return prevalentSystem.hashCode();").dec();
    add("}");
    add("");
    add("@Override");
    add("public String toString() {");
    inc().add("return prevalentSystem.toString();").dec();
    add("}");

  }

  protected void addDecoratorClassUtilities() {
    String interfaceName = repository.getInterfaceType().getCanonicalName();
    add("");
    add("@Override");
    add("public void close() throws java.io.IOException {");
    inc().add("prevayler.close();").dec();
    add("}");
    add("");
    add("public org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler() {");
    inc().add("return prevayler;").dec();
    add("}");
  }

  protected void addDecoratorMethod(Unit unit) {
    String name = unit.getName();
    List<Param> params = unit.getParams();
    if (name.equals("equals") && params.size() == 1 && params.get(0).getType().getBinaryName().equals("java.lang.Object"))
      return;
    if (name.equals("hashCode") && params.isEmpty())
      return;
    if (name.equals("toString") && params.isEmpty())
      return;

    Executable executable = Executable.forUnit(unit);
    Type returns = unit.getReturnType();

    // method head
    add("");
    add("/**");
    add(" * %s", generator.generateMethodDeclaration(unit));
    add(" */ ");
    add("@Override");
    add("%s {", generator.generateMethodDeclaration(unit));
    inc();

    // direct?
    if (unit.getMode() == Mode.DIRECT) {
      String invokeMethodDirect = "prevalentSystem." + name + generator.generateParameterParentheses(unit);
      if (returns.isVoid())
        add("%s;", invokeMethodDirect);
      else {
        add("%s result = %s;", returns, invokeMethodDirect);
        add(returns.isPrimitive() ? "return result;" : "return result == prevalentSystem ? this : result;");
      }
      dec();
      add("}"); // end of method
      return;
    }

    // // instantiate executable/action and let prevayler do the work
    String className = "Executable." + generator.generateClassName(unit);
    String newAction = "new " + className + generator.generateParameterParentheses(unit);
    String assignAction = className + " action = " + newAction;
    String executeAction = "prevayler.execute(action)";
    add(assignAction + ";");

    if (executable.isThrowingException()) {
      add("try {");
      inc();
    }

    if (returns.isVoid()) {
      add(executeAction + ";");
    } else {
      add(returns + " result = " + executeAction + ";");
      add(returns.isPrimitive() ? "return result;" : "return result == prevalentSystem ? this : result;");
    }

    if (executable.isThrowingException()) {
      dec();
      add("} catch (" + merge("", "", " | ", unit.getThrowables()) + " e) {");
      inc().add("throw e;").dec();
      if (!unit.getThrowables().contains(new Type(Exception.class))) {
        add("} catch (java.lang.Exception e) {");
        inc().add("throw new java.lang.RuntimeException(e);").dec();
      }

      add("}");
    }

    dec();
    add("}"); // end of method
  }

  protected void addExecutableClass(Unit unit) {
    String className = generator.generateClassName(unit);
    add("");
    add("class %s implements %s {", className, generator.generateImplements(repository, unit));
    inc();
    Long uid = unit.getSerialVersionUID();
    if (uid == null) {
      uid = random.nextLong();
    }
    add("");
    add("private static final long serialVersionUID = %dL;", uid);
    addExecutableClassFieldsAndConstructor(unit);
    addExecutableImplementation(unit);
    dec();
    add("");
    add("}");
  }

  protected void addExecutableClassFieldsAndConstructor(Unit unit) {
    if (unit.getParams().isEmpty())
      return;
    add("");
    for (Param field : unit.getParams()) {
      if (field.isTime())
        add("@SuppressWarnings(\"unused\")");
      add("private final %s %s;", field.getType(), field.getName());
    }
    add("");
    add("public %s%s {", generator.generateClassName(unit), generator.generateParameterSignature(unit));
    inc();
    for (Param field : unit.getParams()) {
      add("this.%s = %1$s;", field.getName());
    }
    dec();
    add("}");
  }

  protected void addExecutableImplementation(Unit unit) {
    String methodCall = unit.getName() + generator.generateParameterParenthesesWithExecutionTime(unit);
    Executable executable = Executable.forUnit(unit);
    add("");
    add("@Override");
    add("public %s {", executable.toString(unit.getReturnType(), repository.getInterfaceType()));
    inc();
    add("%sprevalentSystem.%s;", executable == Executable.TRANSACTION ? "" : "return ", methodCall);
    dec();
    add("}");
  }

  protected void addPackage() {
    String name = repository.getDecoratorType().getPackageName();
    if (name == null || name.isEmpty())
      return;
    // no empty line before package declaration
    add("package %s;", name);
  }

  @Override
  public Source createSource(Repository repository) {
    this.repository = repository;
    addPackage();
    addClassComment();
    addDecoratorClass();
    return new Source(repository.getDecoratorType().toURI(), list());
  }

}
