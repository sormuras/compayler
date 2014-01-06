package com.github.sormuras.compayler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import com.github.sormuras.compayler.Compayler.Configuration;
import com.github.sormuras.compayler.Compayler.DescriptionFactory;
import com.github.sormuras.compayler.Compayler.DescriptionVisitor;
import com.github.sormuras.compayler.Compayler.DescriptionWriter;
import com.github.sormuras.compayler.Compayler.Kind;
import com.github.sormuras.compayler.Compayler.Mode;
import com.github.sormuras.compayler.Description.Field;

public class Scribe implements DescriptionFactory, DescriptionVisitor, DescriptionWriter {

  private final Configuration configuration;

  public Scribe(Configuration configuration) {
    this.configuration = configuration;
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
    CRC32 crc32 = new CRC32();
    for (Method method : interfaceClass.getMethods()) {
      // simple strings
      String name = method.getName();
      String returnType = method.getReturnType().getCanonicalName();
      // collect exception type names
      List<String> throwables = new ArrayList<>();
      for (Class<?> exceptionType : method.getExceptionTypes())
        throwables.add(exceptionType.getCanonicalName());
      // parse parameters to fields
      crc32.reset();
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
        // update checksum
        crc32.update(field.getType().getBytes());
      }
      // create description
      Description description = new Description(crc32.getValue(), name, returnType, fields, throwables);
      // update mode, if possible
      if (method.isAnnotationPresent(Compayler.Directive.class))
        description.setMode(method.getAnnotation(Compayler.Directive.class).value());
      // done
      descriptions.add(description);
    }
    return descriptions;
  }

  protected String generateClassName(Description description) {
    // capitalize name
    String name = description.getName();
    name = name.toUpperCase().charAt(0) + name.substring(1);
    // done, if name is unique
    if (description.isNameUnique()) {
      return name;
    }
    // name is overloaded, append hash for parameter type name strings
    return name + description.getChecksum(Character.MAX_RADIX);
  }

  protected String generateExceptions(Description description, String separator) {
    StringBuilder builder = new StringBuilder();
    boolean comma = false;
    for (String throwable : description.getThrowables()) {
      if (comma) {
        builder.append(separator);
      }
      builder.append(" ");
      builder.append(throwable);
      comma = true;
    }
    return builder.toString().trim();
  }

  protected String generateImplements(Description description) {
    String interfaceName = configuration.getInterfaceName();
    String typed = "<" + interfaceName + ", " + Compayler.wrap(description.getReturnType()) + ">";
    Kind kind = description.getKind();
    if (kind == Kind.TRANSACTION) {
      typed = "<" + interfaceName + ">";
    }
    return kind.getExecutableInterface().getCanonicalName() + typed;
  }

  protected Kind generateKind(Description description) {
    if (description.getMode() == Mode.QUERY)
      return Kind.QUERY;
    if (description.getReturnType().equals("void"))
      return Kind.TRANSACTION;
    if (description.getThrowables().isEmpty())
      return Kind.TRANSACTION_QUERY;
    // fallen through
    return Kind.TRANSACTION_QUERY_EXCEPTION;
  }

  protected String generateMethodDeclaration(Description description) {
    StringBuilder builder = new StringBuilder();
    builder.append(description.getReturnType());
    builder.append(" ");
    builder.append(description.getName());
    builder.append(generateParameterSignature(description));
    if (description.getThrowables().isEmpty())
      return builder.toString();

    builder.append(" throws ");
    builder.append(generateExceptions(description, ","));
    return builder.toString();
  }

  protected Map<String, Boolean> generateNameIsUniqueMap(List<Description> descriptions) {
    Map<String, Boolean> uniques = new HashMap<>();
    for (Description description : descriptions) {
      String name = description.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old == null)
        continue;
      uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  protected String generateParameterParentheses(Description description) {
    int length = description.getFields().size();
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + description.getFields().get(0).getName() + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(description.getFields().get(0).getName());
    for (int index = 1; index < length; index++) {
      builder.append(", ").append(description.getFields().get(index).getName());
    }
    builder.append(")");
    return builder.toString();
  }

  protected String generateParameterParenthesesWithExecutionTime(Description description) {
    String parantheses = generateParameterParentheses(description);
    for (Field field : description.getFields()) {
      if (field.isTime())
        return parantheses.replace(field.getName(), "executionTime");
    }
    return parantheses;
  }

  protected String generateParameterSignature(Description description) {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (Field field : description.getFields()) {
      if (field.getIndex() > 0)
        builder.append(", ");
      if (field.isVariable())
        builder.append(Compayler.replaceLast(field.getType(), "[]", "..."));
      else
        builder.append(field.getType());
      builder.append(' ').append(field.getName());
    }
    builder.append(')');
    return builder.toString();
  }

  @Override
  public void visitDescriptions(List<Description> descriptions) {
    Map<String, Boolean> uniques = generateNameIsUniqueMap(descriptions);
    for (Description description : descriptions) {
      description.setNameUnique(uniques.get(description.getName()));
      description.setClassName(generateClassName(description));
      description.setKind(generateKind(description));
      description.setMethodDeclaration(generateMethodDeclaration(description));
      description.setParameterParentheses(generateParameterParentheses(description));
    }
  }

  @Override
  public Source writeDecorator(List<Description> descriptions) {
    List<String> lines = new LinkedList<>();
    lines.add("package " + configuration.getTargetPackage() + ";");

    String interfaceName = configuration.getInterfaceName();
    String className = configuration.getDecoratorName();

    // head
    lines.add("");
    lines.add("/**");
    lines.add(" * @compayled " + Compayler.now());
    lines.add(" */");
    lines.add("public class " + className + " implements " + interfaceName + ", java.lang.AutoCloseable {");

    // fields
    lines.add("");
    lines.add("  protected final org.prevayler.Prevayler<" + interfaceName + "> prevayler;");
    lines.add("  protected final " + interfaceName + " prevalentSystem;");

    // c'tor
    lines.add("");
    lines.add("  public " + className + "(org.prevayler.Prevayler<" + interfaceName + "> prevayler) {");
    lines.add("    this.prevayler = prevayler;");
    lines.add("    this.prevalentSystem = prevayler.prevalentSystem();");
    lines.add("  }");

    // utility methods
    lines.add("");
    lines.add("  @Override");
    lines.add("  public void close() throws java.io.IOException {");
    lines.add("    prevayler.close();");
    lines.add("  }");
    lines.add("");
    lines.add("  public org.prevayler.Prevayler<" + interfaceName + "> prevayler() {");
    lines.add("    return prevayler;");
    lines.add("  }");
    lines.add("");
    lines.add("  protected " + interfaceName + " redirect(" + interfaceName + " result) {");
    lines.add("    return result == prevalentSystem ? this : result;");
    lines.add("  }");

    // decorating methods
    for (Description description : descriptions) {

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
      String fullClassName = description.getClassName();
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
          lines.add("    " + "} catch (" + generateExceptions(description, "|") + " e) {");
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
    lines.add(classModifier + " class " + description.getClassName() + " implements " + generateImplements(description) + " {");

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
      lines.add("  public " + description.getClassName() + generateParameterSignature(description) + " {");
      for (Field field : description.getFields()) {
        lines.add("    this." + field.getName() + " = " + field.getName() + ";");
      }
      lines.add("  }");
    } // end of fields + c'tor

    // implementation
    String parameters = configuration.getInterfaceName() + " prevalentSystem, java.util.Date executionTime";
    String returnType = Compayler.wrap(description.getReturnType());
    String methodCall = description.getName() + generateParameterParenthesesWithExecutionTime(description);
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
