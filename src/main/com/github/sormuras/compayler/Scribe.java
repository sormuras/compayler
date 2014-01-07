package com.github.sormuras.compayler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.sormuras.compayler.Compayler.Configuration;
import com.github.sormuras.compayler.Compayler.DescriptionFactory;
import com.github.sormuras.compayler.Compayler.DescriptionWriter;
import com.github.sormuras.compayler.Compayler.Kind;
import com.github.sormuras.compayler.Compayler.Mode;
import com.github.sormuras.compayler.Description.Field;
import com.github.sormuras.compayler.Description.Signature;

public class Scribe implements DescriptionFactory, DescriptionWriter {

  private final Configuration configuration;

  public Scribe(Configuration configuration) {
    this.configuration = configuration;
  }

  protected Map<String, Boolean> buildNameIsUniqueMap(Class<?> interfaceClass) {
    Map<String, Boolean> uniques = new HashMap<>();
    for (Method method : interfaceClass.getMethods()) {
      String name = method.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old == null)
        continue;
      uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  @Override
  public List<Description> createDescriptions() {
    List<Description> descriptions = new ArrayList<>();
    Class<?> interfaceClass;
    try {
      interfaceClass = Class.forName(configuration.getInterfaceName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Can't describe " + configuration, e);
    }
    Map<String, Boolean> uniques = buildNameIsUniqueMap(interfaceClass);
    for (Method method : interfaceClass.getMethods()) {
      // simple strings
      String name = method.getName();
      String returnType = method.getReturnType().getCanonicalName();
      // collect exception type names
      List<String> throwables = new ArrayList<>();
      for (Class<?> exceptionType : method.getExceptionTypes())
        throwables.add(exceptionType.getCanonicalName());
      // parse parameters to fields
      List<Field> fields = new ArrayList<>();
      int lastIndex = method.getParameterTypes().length - 1;
      for (int index = 0; index <= lastIndex; index++) {
        Field field = new Field();
        field.setIndex(index);
        field.setName("p" + index);
        field.setTime(Compayler.isExecutionTimePresent(method.getParameterAnnotations()[index]));
        field.setType(method.getParameterTypes()[index].getCanonicalName());
        field.setVariable(index == lastIndex && method.isVarArgs());
        fields.add(field);
      }
      // create description
      Signature signature = new Signature(name, returnType, fields, throwables, uniques.get(name));
      Description description = new Description(configuration, signature);
      // update mode, if possible
      if (method.isAnnotationPresent(Compayler.Directive.class))
        description.getVariable().setMode(method.getAnnotation(Compayler.Directive.class).value());
      // done
      descriptions.add(description);
    }
    return descriptions;
  }

  @Override
  public Source writeDecorator(List<Description> descriptions) {
    List<String> lines = new LinkedList<>();
    lines.add("package " + configuration.getTargetPackage() + ";");

    String typeVariables = Compayler.merge(Arrays.asList(configuration.getInterfaceTypeVariables()));
    String interfaceName = configuration.getInterfaceName() + typeVariables;
    String className = configuration.getDecoratorName();

    // head
    lines.add("");
    lines.add("/**");
    lines.add(" * @compayled " + Compayler.now());
    lines.add(" */");
    lines.add("public class " + className + typeVariables + " implements " + interfaceName + ", java.lang.AutoCloseable {");

    // fields
    lines.add("");
    lines.add("  protected final org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler;");
    lines.add("  protected final " + interfaceName + " prevalentSystem;");

    // c'tor
    lines.add("");
    lines.add("  public " + className + "(org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler) {");
    lines.add("    this.prevayler = prevayler;");
    lines.add("    this.prevalentSystem = prevayler.prevalentSystem();");
    lines.add("  }");

    // hashCode(), equals() and toString() methods declared in java.lang.Object
    lines.add("");
    lines.add("  @Override");
    lines.add("  public boolean equals(Object obj) {");
    lines.add("    return prevalentSystem.equals(obj);");
    lines.add("  }");
    lines.add("");
    lines.add("  @Override");
    lines.add("  public int hashCode() {");
    lines.add("    return prevalentSystem.hashCode();");
    lines.add("  }");
    lines.add("");
    lines.add("  @Override");
    lines.add("  public String toString() {");
    lines.add("    return prevalentSystem.toString();");
    lines.add("  }");

    // utility methods
    lines.add("");
    lines.add("  @Override");
    lines.add("  public void close() throws java.io.IOException {");
    lines.add("    prevayler.close();");
    lines.add("  }");
    lines.add("");
    lines.add("  public org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler() {");
    lines.add("    return prevayler;");
    lines.add("  }");
    lines.add("");
    lines.add("  protected " + interfaceName + " redirect(" + interfaceName + " result) {");
    lines.add("    return result == prevalentSystem ? this : result;");
    lines.add("  }");

    // decorating methods
    for (Description description : descriptions) {

      if (description.getName().equals("equals") && description.getFields().size() == 1
          && description.getFields().get(0).getType().equals("java.lang.Object"))
        continue;
      if (description.getName().equals("hashCode") && description.getFields().isEmpty())
        continue;
      if (description.getName().equals("toString") && description.getFields().isEmpty())
        continue;

      Kind kind = description.getKind();
      String returns = description.getReturnType();

      // method head
      lines.add("");
      boolean redirect = returns.equals(interfaceName);
      // Generate javadoc for decorated method.
      lines.add("  /**");
      lines.add("   * " + description.getClassName());
      lines.add("   */ ");
      lines.add("  @Override");
      lines.add("  public " + description.getMethodDeclaration() + " {");

      // direct?
      if (description.getMode() == Mode.DIRECT) {
        String invokeMethodDirect = "prevalentSystem." + description.getName() + description.getParameterParentheses();
        if ("void".equals(returns))
          lines.add("    " + invokeMethodDirect + ";");
        else if (redirect)
          lines.add("    return redirect(" + invokeMethodDirect + ");");
        else
          lines.add("    return " + invokeMethodDirect + ";");

        lines.add("  }"); // end of method
        continue;
      }

      // instantiate executable/action and let prevayler do the work
      String fullClassName = description.getClassNameWithTypeVariables();
      String newAction = "new " + fullClassName + description.getParameterParentheses();
      String assignAction = fullClassName + " action = " + newAction;
      String executeAction = "prevayler.execute(action)";
      lines.add("    " + assignAction + ";");

      if (kind.isThrowingException()) {
        lines.add("    " + "try {");
      }

      String innerIndent = kind.isThrowingException() ? "    " + "  " : "    ";
      if ("void".equals(returns)) {
        lines.add(innerIndent + executeAction + ";");
      } else {
        lines.add(innerIndent + returns + " result = " + executeAction + ";");
        if (redirect) {
          lines.add(innerIndent + "return redirect(result);");
        } else
          lines.add(innerIndent + "return result;");
      }

      if (kind.isThrowingException()) {
        if (!description.getThrowables().isEmpty()) {
          lines.add("    " + "} catch (" + description.getExceptions("|") + " e) {");
          lines.add("    " + "  throw e;");
        }
        if (!description.getThrowables().contains("java.lang.Exception")) {
          lines.add("    " + "} catch (java.lang.Exception e) {");
          lines.add("    " + "  throw new RuntimeException(e);");
        }
        lines.add("    " + "}");
      }

      lines.add("  }"); // end of method
    }

    for (Description description : descriptions) {
      writeExecutable(description, lines, "protected static");
    }

    lines.add("");
    lines.add("}");
    lines.add("");

    return new Source(configuration.getTargetPackage(), configuration.getDecoratorName(), lines);
  }

  @Override
  public Source writeExecutable(Description description) {
    List<String> lines = new LinkedList<>();
    lines.add("");
    lines.add("// " + getClass().getSimpleName() + " wrote " + description.getClassName() + " on " + Compayler.now());

    lines.add("");
    lines.add("package " + configuration.getTargetPackage() + ";");

    writeExecutable(description, lines, "public");

    return new Source(configuration.getTargetPackage(), description.getClassName(), lines);
  }

  protected void writeExecutable(Description description, List<String> lines, String classModifier) {    
    lines.add("");
    lines.add(classModifier + " class " + description.getClassNameWithTypeVariables() + " implements " + description.getImplements() + " {");

    lines.add("");
    lines.add("  private static final long serialVersionUID = " + description.getSerialVersionUID() + "L;");

    // fields + c'tor, if at least one field is present
    if (!description.getFields().isEmpty()) {
      lines.add("");
      for (Field field : description.getFields()) {
        if (field.isTime())
          lines.add("  @SuppressWarnings(\"unused\")");
        lines.add("  private final " + field.getType() + " " + field.getName() + ";");
      }
      lines.add("");
      lines.add("  public " + description.getClassName() + description.getParameterSignature() + " {");
      for (Field field : description.getFields()) {
        lines.add("    this." + field.getName() + " = " + field.getName() + ";");
      }
      lines.add("  }");
    } // end of fields + c'tor

    // implementation
    String typeVariables = Compayler.merge(Arrays.asList(configuration.getInterfaceTypeVariables()));
    String parameters = configuration.getInterfaceName() + typeVariables + " prevalentSystem, java.util.Date executionTime";
    String returnType = Compayler.wrap(description.getReturnType());
    String methodCall = description.getName() + description.getParameterParenthesesWithExecutionTime();
    lines.add("");
    lines.add("  @Override");
    switch (description.getKind()) {
    case QUERY:
      lines.add("  public " + returnType + " query(" + parameters + ") throws java.lang.Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    case TRANSACTION:
      lines.add("  public void executeOn(" + parameters + ") {");
      lines.add("    prevalentSystem." + methodCall + ";");
      break;
    case TRANSACTION_QUERY:
      lines.add("  public " + returnType + " executeAndQuery(" + parameters + ") {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    case TRANSACTION_QUERY_EXCEPTION:
      lines.add("  public " + returnType + " executeAndQuery(" + parameters + ") throws java.lang.Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    }
    lines.add("  }"); // end of implementation

    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
  }

}
