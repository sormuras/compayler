package de.sormuras.compayler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

import de.sormuras.compayler.Descriptor.Field;

/**
 * Prevayler Decorator Compiler.
 * 
 * @author Christian Stein
 */
public class Compayler {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface CompaylerHint {

    Mode value() default Mode.TRANSACTION;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public static @interface ExecutionTime {
    // empty
  }

  public static enum Kind {

    QUERY(Mode.QUERY, Query.class, true),

    TRANSACTION(Mode.TRANSACTION, Transaction.class, false),

    TRANSACTION_QUERY(Mode.TRANSACTION, SureTransactionWithQuery.class, false),

    TRANSACTION_QUERY_EXCEPTION(Mode.TRANSACTION, TransactionWithQuery.class, true);

    private final Class<?> executableInterface;
    private final Mode mode;
    private final boolean throwingException;

    private Kind(Mode mode, Class<?> executableInterface, boolean throwingException) {
      this.mode = mode;
      this.executableInterface = executableInterface;
      this.throwingException = throwingException;
    }

    public Class<?> getExecutableInterface() {
      return executableInterface;
    }

    public Mode getMode() {
      return mode;
    }

    public boolean isThrowingException() {
      return throwingException;
    }

  }

  public static enum Mode {
    DIRECT, QUERY, TRANSACTION
  }

  public static interface Parser {

    List<Descriptor> parse(String className);

  }

  public static String buildClassName(Descriptor descriptor) {
    // capitalize unit name
    String name = descriptor.getName();
    name = name.toUpperCase().charAt(0) + name.substring(1);
    // TODO Append Direct, Query or Transaction to class or package name?
    // Mode mode = descriptor.getMode();
    // name = name + mode.name().charAt(0) + mode.name().substring(1).toLowerCase(); // Transaction | Query | Direct
    // done, if name is unique
    if (descriptor.isUnique()) {
      return name;
    }
    // name's not unique, append hash for parameter type name strings
    return name + "_" + buildHashOfTypeNames(descriptor);
  }

  public static String buildExceptions(Descriptor descriptor, String separator) {
    StringBuilder builder = new StringBuilder();
    boolean comma = false;
    for (String throwable : descriptor.getThrowables()) {
      if (comma) {
        builder.append(separator);
      }
      builder.append(" ");
      builder.append(throwable);
      comma = true;
    }
    return builder.toString().trim();
  }

  public static String buildHashOfTypeNames(Descriptor descriptor) {
    StringBuilder builder = new StringBuilder();
    for (Field field : descriptor.getFields()) {
      String name = field.getType();
      // TODO Remove generic declaration from type name?
      // if (name.indexOf('<') > 0) name = name.substring(0, name.indexOf('<'));
      builder.append(name);
    }
    CRC32 checksum = new CRC32();
    checksum.update(builder.toString().getBytes());
    return Long.toString(checksum.getValue(), Character.MAX_RADIX).toUpperCase();
  }

  public static String buildImplements(String interfaceName, Descriptor descriptor) {
    String typed = "<" + interfaceName + ", " + wrap(descriptor.getReturnType()) + ">";
    Kind kind = buildKind(descriptor);
    if (kind == Kind.TRANSACTION) {
      typed = "<" + interfaceName + ">";
    }
    return kind.getExecutableInterface().getCanonicalName() + typed;
  }

  public static Kind buildKind(Descriptor descriptor) {
    if (descriptor.getMode() == Mode.QUERY)
      return Kind.QUERY;
    if (descriptor.getReturnType().equals("void"))
      return Kind.TRANSACTION;
    if (descriptor.getThrowables().isEmpty())
      return Kind.TRANSACTION_QUERY;
    // default
    return Kind.TRANSACTION_QUERY_EXCEPTION;
  }

  public static String buildMethodDeclaration(Descriptor descriptor) {
    StringBuilder builder = new StringBuilder();
    builder.append(descriptor.getReturnType());
    builder.append(" ");
    builder.append(descriptor.getName());
    builder.append(buildParameterSignature(descriptor));
    if (descriptor.getThrowables().isEmpty())
      return builder.toString();

    builder.append(" throws ");
    builder.append(buildExceptions(descriptor, ","));
    return builder.toString();
  }

  public static String buildParameterParentheses(Descriptor descriptor) {
    int length = descriptor.getFields().size();
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + descriptor.getFields().get(0).getName() + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(descriptor.getFields().get(0).getName());
    for (int i = 1; i < length; i++) {
      builder.append(", ").append(descriptor.getFields().get(i).getName());
    }
    builder.append(")");
    return builder.toString();
  }

  public static String buildParameterParenthesesWithExecutionTime(Descriptor descriptor) {
    String parantheses = buildParameterParentheses(descriptor);
    for (Field field : descriptor.getFields()) {
      if (field.isTime())
        return parantheses.replace(field.getName(), "executionTime");
    }
    return parantheses;
  }

  public static String buildParameterSignature(Descriptor descriptor) {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (Field field : descriptor.getFields()) {
      if (!field.isFirst())
        builder.append(", ");
      if (field.isVariable())
        builder.append(replaceLast(field.getType(), "[]", "..."));
      else
        builder.append(field.getType());
      builder.append(' ').append(field.getName());
    }
    builder.append(')');
    return builder.toString();
  }

  public static Source createDecorator(String interfaceName, String className, Descriptor... descriptors) {
    List<String> lines = new LinkedList<>();

    String packageName = descriptors[0].getPackageName();
    String superName = Decorator.class.getCanonicalName();

    lines.add("package " + packageName + ";");

    lines.add("");
    lines.add("public class " + className + " extends " + superName + "<" + interfaceName + "> implements " + interfaceName + " {");
    lines.add("");
    lines.add("  public " + className + "(org.prevayler.Prevayler<" + interfaceName + "> prevayler) {");
    lines.add("    super(prevayler);");
    lines.add("  }");

    for (Descriptor descriptor : descriptors) {

      Kind kind = buildKind(descriptor);

      // method head
      lines.add("");
      boolean redirect = descriptor.getReturnType().equals(interfaceName);
      // Generate javadoc for decorated method.
      lines.add("  /**");
      lines.add("   * " + descriptor.getClassName());
      lines.add("   */ ");
      lines.add("  @Override");
      lines.add("  public " + buildMethodDeclaration(descriptor) + " {");

      // direct?
      if (descriptor.getMode() == Mode.DIRECT) {
        String invokeMethodDirect = "prevalentSystem." + descriptor.getName() + buildParameterParentheses(descriptor);
        if ("void".equals(descriptor.getReturnType()))
          lines.add("    " + invokeMethodDirect + ";");
        else if (redirect)
          lines.add("    return redirect(" + invokeMethodDirect + ");");
        else
          lines.add("    return " + invokeMethodDirect + ";");

        lines.add("  }"); // end of method
        continue;
      }

      // // instantiate executable/action and let prevayler do the work
      String newAction = "new " + descriptor.getClassName() + buildParameterParentheses(descriptor);
      String assignAction = descriptor.getClassName() + " action = " + newAction;
      String executeAction = "prevayler.execute(action)";
      lines.add("    " + assignAction + ";");

      if (kind.isThrowingException()) {
        lines.add("    " + "try {");
      }

      String innerIndent = kind.isThrowingException() ? "    " + "  " : "    ";
      if ("void".equals(descriptor.getReturnType())) {
        lines.add(innerIndent + executeAction + ";");
      } else {
        lines.add(innerIndent + descriptor.getReturnType() + " result = " + executeAction + ";");
        if (redirect) {
          lines.add(innerIndent + "return redirect(result);");
        } else
          lines.add(innerIndent + "return result;");
      }

      if (kind.isThrowingException()) {
        if (!descriptor.getThrowables().isEmpty()) {
          lines.add("    " + "} catch (" + buildExceptions(descriptor, "|") + " e) {");
          lines.add("    " + "  throw e;");
        }
        if (!descriptor.getThrowables().contains("java.lang.Exception")) {
          lines.add("    " + "} catch (java.lang.Exception e) {");
          lines.add("    " + "  throw new RuntimeException(e);");
        }
        lines.add("    " + "}");
      }

      lines.add("  }"); // end of method
    }

    lines.add("");
    lines.add("}");
    lines.add("");

    return new Source(packageName, className, lines);
  }

  public static Source createExecutableSource(String interfaceName, Descriptor descriptor) {
    List<String> lines = new ArrayList<>();
    lines.add("package " + descriptor.getPackageName() + ";");

    lines.add("");
    lines.add("public class " + descriptor.getClassName() + " implements " + buildImplements(interfaceName, descriptor) + " {");

    lines.add("");
    lines.add("  private static final long serialVersionUID = " + descriptor.getSerialVersionUID() + "L;");

    // fields + c'tor, if at least one field is present
    if (descriptor.hasFields()) {
      lines.add("");
      for (Field field : descriptor.getFields()) {
        if (field.isTime())
          lines.add("  @SuppressWarnings(\"unused\")");
        lines.add("  private final " + field.getType() + " " + field.getName() + ";");
      }
      lines.add("");
      lines.add("  public " + descriptor.getClassName() + buildParameterSignature(descriptor) + " {");
      for (Field field : descriptor.getFields()) {
        lines.add("    this." + field.getName() + " = " + field.getName() + ";");
      }
      lines.add("  }");
    } // end of fields + c'tor

    // implementation
    String parameters = interfaceName + " prevalentSystem, java.util.Date executionTime";
    String returnType = wrap(descriptor.getReturnType());
    String methodCall = descriptor.getName() + buildParameterParenthesesWithExecutionTime(descriptor);
    lines.add("");
    lines.add("  @Override");
    switch (buildKind(descriptor)) {
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
    return new Source(descriptor.getPackageName(), descriptor.getClassName(), lines);
  }

  public static String replaceLast(String string, String toReplace, String replacement) {
    int pos = string.lastIndexOf(toReplace);
    if (pos < 0)
      return string;
    return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
  }

  public static void updateClassNames(Collection<Descriptor> descriptors) {
    for (Descriptor descriptor : descriptors)
      descriptor.setClassName(buildClassName(descriptor));
  }

  public static void updateUniqueFlags(Collection<Descriptor> descriptors) {
    Map<String, Boolean> uniques = new HashMap<>();
    for (Descriptor descriptor : descriptors) {
      String name = descriptor.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old != null)
        uniques.put(name, Boolean.FALSE);
    }
    for (Descriptor descriptor : descriptors)
      descriptor.setUnique(uniques.get(descriptor.getName()));
  }

  public static String wrap(String className) {
    switch (className) {
    case "boolean":
      return "java.lang.Boolean";
    case "byte":
      return "java.lang.Byte";
    case "char":
      return "java.lang.Character";
    case "double":
      return "java.lang.Double";
    case "float":
      return "java.lang.Float";
    case "int":
      return "java.lang.Integer";
    case "long":
      return "java.lang.Long";
    case "short":
      return "java.lang.Short";
    case "void":
      return "java.lang.Void";
    default:
      return className;
    }
  }

}
