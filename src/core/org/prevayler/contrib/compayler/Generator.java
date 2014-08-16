package org.prevayler.contrib.compayler;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static java.util.Collections.sort;

import java.io.Closeable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import javax.annotation.Generated;

import org.prevayler.contrib.compayler.Compayler.ExecutionMode;
import org.prevayler.contrib.compayler.Unit.Parameter;

/**
 * Generates java source code
 * 
 * @author Sor
 */
public class Generator {

  /**
   * Prevayler action/executable interface information.
   */
  public enum Executable {

    /**
     * Represents an atomic query that can be executed on a Prevalent System that returns a result or throws an Exception after executing.
     */
    QUERY("Query", "%s query", true),

    /**
     * An atomic transaction to be executed on a Prevalent System.
     */
    TRANSACTION("Transaction", "%s executeOn", false),

    /**
     * An atomic transaction that also returns a result.
     */
    TRANSACTION_QUERY("SureTransactionWithQuery", "%s executeAndQuery", false),

    /**
     * An atomic transaction that also returns a result or throws an Exception after executing.
     */
    TRANSACTION_QUERY_EXCEPTION("TransactionWithQuery", "%s executeAndQuery", true);

    public static Executable forUnit(Unit unit) {
      if (unit.getMode() == ExecutionMode.QUERY)
        return QUERY;
      if (unit.getReturns().equals("void"))
        return TRANSACTION;
      if (unit.getThrowns().isEmpty())
        return TRANSACTION_QUERY;
      // if nothing applies...
      return TRANSACTION_QUERY_EXCEPTION;
    }

    private final String methodFormat;
    private final boolean throwingException;
    private final String type;

    private Executable(String executableTypeName, String methodFormat, boolean throwingException) {
      this.type = "org.prevayler." + executableTypeName;
      this.throwingException = throwingException;
      String signature = "(%s prevalentSystem, java.util.Date executionTime)" + (throwingException ? " throws java.lang.Exception" : "");
      this.methodFormat = methodFormat + signature;
    }

    public String getMethodFormat() {
      return methodFormat;
    }

    public String getType() {
      return type;
    }

    public boolean isThrowingException() {
      return throwingException;
    }

    public String toString(String returnType, String prevalentSystem) {
      return String.format(getMethodFormat(), canonical(returnType, this != TRANSACTION), prevalentSystem);
    }

  }

  private static final String[] INDENT;

  private static final int INDENT_MAX = 16;

  public static final String INDENTATION = "  ";

  static {
    INDENT = new String[INDENT_MAX];
    for (int i = 0; i < INDENT_MAX; i++)
      INDENT[i] = join("", nCopies(i, INDENTATION));
  }

  public static String canonical(String canonicalName, boolean wrap) {
    return wrap ? primitive(canonicalName) ? wrap(canonicalName).getCanonicalName() : canonicalName : canonicalName;
  }

  /**
   * @return true if this object represents a simple data type; false otherwise.
   * @see Class#isPrimitive()
   */
  public static boolean primitive(String binaryName) {
    return binaryName.equals("boolean") || binaryName.equals("byte") || binaryName.equals("char") || binaryName.equals("double")
        || binaryName.equals("float") || binaryName.equals("int") || binaryName.equals("long") || binaryName.equals("short")
        || binaryName.equals("void");
  }

  public static String simple(String canoncicalName) {
    if (canoncicalName.startsWith("[") || canoncicalName.contains("$"))
      throw new IllegalArgumentException(canoncicalName + " isn't canonical!");
    int i = canoncicalName.lastIndexOf('.');
    return i < 0 ? canoncicalName : canoncicalName.substring(i + 1);
  }

  public static Class<?> wrap(String name) {
    switch (name) {
    case "boolean":
      return Boolean.class;
    case "byte":
      return Byte.class;
    case "char":
      return Character.class;
    case "double":
      return Double.class;
    case "float":
      return Float.class;
    case "int":
      return Integer.class;
    case "long":
      return Long.class;
    case "short":
      return Short.class;
    case "void":
      return Void.class;
    }
    throw new IllegalArgumentException(name + " isn't a primitive type!");
  }

  protected final StringBuilder builder = new StringBuilder();
  protected final Compayler compayler;
  protected final CRC32 crc32 = new CRC32();
  protected int indentationDepth = 0;
  protected final List<String> lines = new ArrayList<>();
  protected final Random random = new Random();
  protected final List<Unit> units;
  protected final Instant now = Instant.now();

  public Generator(Compayler compayler, List<Unit> units) {
    this.compayler = compayler;
    this.units = units;
  }

  protected Generator add(String format, Object... args) {
    if (format.isEmpty()) {
      lines.add("");
      return this;
    }
    builder.setLength(0);
    builder.append(INDENT[indentationDepth]);
    builder.append(args.length == 0 ? format : format(format, args));
    lines.add(builder.toString());
    return this;
  }

  protected void addClassComment() {
    if (!lines.isEmpty())
      add("");
    add("/**");
    add(" * Class %s generated for %s.", compayler.getDecoratorName(), compayler.getInterfaceName());
    add(" *");
    add(" * Generated on %s.", now);
    add(" */");
  }

  protected void addDecoratorClass() {
    StringBuilder line = new StringBuilder();
    add("@" + Generated.class.getCanonicalName() + "(value=\"" + getClass().getCanonicalName() + "\", date=\"" + now + "\")");
    line.append("public class ").append(simple(compayler.getDecoratorName()));
    line.append(" extends ").append(compayler.getSuperClass().getCanonicalName());
    line.append(" implements ").append(join(", ", Closeable.class.getCanonicalName(), compayler.getInterfaceName()));
    line.append(" {");
    add(line.toString());
    inc();

    sort(units, (o1, o2) -> o1.getName().compareTo(o2.getName()));

    add("");
    add("public interface Executable {");
    inc();
    for (Unit unit : units) {
      if (unit.isDefaults())
        continue;
      if (unit.getName().equals("close") && unit.getParameters().isEmpty() && unit.getReturns().equals("void"))
        continue;
      addExecutableClass(unit);
    }
    dec();
    add("");
    add("}"); // end of interface

    addDecoratorClassFieldsAndConstructor();
    addDecoratorClassObjectMethods();
    addDecoratorClassUtilities();
    for (Unit unit : units) {
      if (unit.isDefaults())
        continue;
      addDecoratorMethod(unit);
    }

    dec();
    add("");
    add("}"); // end of decorator class
  }

  protected void addDecoratorClassFieldsAndConstructor() {
    String interfaceName = compayler.getInterfaceName();
    add("");
    add("protected final org.prevayler.Prevayler<? extends %s> prevayler;", interfaceName);
    add("protected final %s prevalentSystem;", interfaceName);
    add("");
    add("public %s(org.prevayler.Prevayler<? extends %s> prevayler) {", simple(compayler.getDecoratorName()), interfaceName);
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
    String interfaceName = compayler.getInterfaceName();
    add("");
    add("@Override");
    add("public void close() throws java.io.IOException {");
    inc();
    add("prevayler.close();");
    add("if (prevalentSystem instanceof java.io.Closeable)");
    inc();
    add("((java.io.Closeable) prevalentSystem).close();");
    dec();
    dec();
    add("}");
    add("");
    add("public org.prevayler.Prevayler<? extends " + interfaceName + "> prevayler() {");
    inc().add("return prevayler;").dec();
    add("}");
  }

  protected void addDecoratorMethod(Unit unit) {
    String name = unit.getName();
    List<Parameter> params = unit.getParameters();
    if (name.equals("equals") && params.size() == 1 && params.get(0).getType().equals("java.lang.Object"))
      return;
    if (name.equals("hashCode") && params.isEmpty())
      return;
    if (name.equals("toString") && params.isEmpty())
      return;
    if (name.equals("close") && params.isEmpty())
      return;

    Executable executable = Executable.forUnit(unit);
    String returns = unit.getReturns();

    // method head
    add("");
    add("/**");
    add(" * %s", generateMethodDeclaration(unit));
    add(" */ ");
    add("@Override");
    add("%s {", generateMethodDeclaration(unit));
    inc();

    // direct?
    if (unit.getMode() == ExecutionMode.DIRECT) {
      String invokeMethodDirect = "prevalentSystem." + name + generateParameterParentheses(unit);
      if (returns.equals("void"))
        add("%s;", invokeMethodDirect);
      else {
        add("%s result = %s;", returns, invokeMethodDirect);
        add(unit.isChainable() ? "return result == prevalentSystem ? this : result;" : "return result;");
      }
      dec();
      add("}"); // end of method
      return;
    }

    // instantiate executable/action and let prevayler do the work
    String className = "Executable." + generateClassName(unit);
    String newAction = "new " + className + generateParameterParentheses(unit);
    String assignAction = className + " action = " + newAction;
    String executeAction = "prevayler.execute(action)";
    add(assignAction + ";");

    if (executable.isThrowingException()) {
      add("try {");
      inc();
    }

    if (returns.equals("void")) {
      add(executeAction + ";");
    } else {
      add(returns + " result = " + executeAction + ";");
      add(unit.isChainable() ? "return result == prevalentSystem ? this : result;" : "return result;");
    }

    if (executable.isThrowingException()) {
      dec();
      if (!unit.getThrowns().isEmpty()) {
        add("} catch (" + join(" | ", unit.getThrowns()) + " e) {");
        inc().add("throw e;").dec();
      }
      if (!unit.getThrowns().contains(Exception.class.getCanonicalName())) {
        add("} catch (java.lang.Exception e) {");
        inc().add("throw new java.lang.RuntimeException(e);").dec();
      }

      add("}");
    }

    dec();
    add("}"); // end of method
  }

  protected void addPackage() {
    String name = compayler.getDecoratorName();
    int lastDot = name.lastIndexOf('.');
    if (lastDot <= 0)
      return;
    // no empty line before package declaration
    add("package %s;", name.substring(0, lastDot));
  }

  protected void addExecutableClass(Unit unit) {
    String className = generateClassName(unit);
    add("");
    add("class %s implements %s {", className, generateImplements(unit));
    inc();
    Long uid = unit.getSerialVersionUID();
    add("");
    add("private static final long serialVersionUID = %dL;", uid != null ? uid : generateChecksum(unit));
    addExecutableClassFieldsAndConstructor(unit);
    addExecutableImplementation(unit);
    dec();
    add("");
    add("}");
  }

  protected void addExecutableClassFieldsAndConstructor(Unit unit) {
    if (unit.getParameters().isEmpty())
      return;
    add("");
    for (Parameter field : unit.getParameters()) {
      if (field.isTime())
        add("@SuppressWarnings(\"unused\")");
      add("private final %s %s;", field.getType(), field.getName());
    }
    add("");
    add("public %s%s {", generateClassName(unit), generateParameterSignature(unit));
    inc();
    for (Parameter field : unit.getParameters()) {
      add("this.%s = %1$s;", field.getName());
    }
    dec();
    add("}");
  }

  protected void addExecutableImplementation(Unit unit) {
    String methodCall = unit.getName() + generateParameterParentheses(unit, true);
    Executable executable = Executable.forUnit(unit);
    add("");
    add("@Override");
    add("public %s {", executable.toString(unit.getReturns(), compayler.getInterfaceName()));
    inc();
    add("%sprevalentSystem.%s;", executable == Executable.TRANSACTION ? "" : "return ", methodCall);
    dec();
    add("}");
  }

  protected Generator dec() {
    indentationDepth--;
    if (indentationDepth < 0)
      indentationDepth = 0;
    return this;
  }

  protected long generateChecksum(Unit unit) {
    crc32.reset();
    for (Parameter param : unit.getParameters()) {
      crc32.update(param.getType().getBytes());
    }
    crc32.update(unit.getName().getBytes());
    return crc32.getValue();
  }

  protected String generateClassName(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append(unit.getName().toUpperCase().charAt(0));
    builder.append(unit.getName().substring(1));
    if (!unit.isUnique())
      builder.append(Long.toString(generateChecksum(unit), Character.MAX_RADIX).toUpperCase());
    return builder.toString();
  }

  protected String generateImplements(Unit unit) {
    StringBuilder builder = new StringBuilder();
    Executable executable = Executable.forUnit(unit);
    builder.append(executable.getType());
    builder.append('<');
    builder.append(compayler.getInterfaceName());
    if (executable != Executable.TRANSACTION) {
      builder.append(',').append(' ').append(canonical(unit.getReturns(), true));
    }
    builder.append('>');
    return builder.toString();
  }

  protected String generateMethodDeclaration(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append("public");
    builder.append(" ");
    builder.append(unit.getReturns());
    builder.append(" ");
    builder.append(unit.getName());
    builder.append("(");
    String params = join(", ", unit.getParameters().stream().map(p -> p.toString()).collect(Collectors.toList()));
    builder.append(params);

    builder.append(")");
    if (unit.getThrowns().isEmpty())
      return builder.toString();
    builder.append(" throws ");
    builder.append(join(", ", unit.getThrowns()));
    return builder.toString();
  }

  protected String generateParameterParentheses(Unit unit) {
    return generateParameterParentheses(unit, false);
  }

  protected String generateParameterParentheses(Unit unit, boolean withExecutionTime) {
    List<Parameter> params = unit.getParameters();
    int length = params.size();
    if (length == 0)
      return "()";
    Parameter param0 = params.get(0);
    if (length == 1) {
      return "(" + ((withExecutionTime && param0.isTime()) ? "executionTime" : param0.getName()) + ")";
    }
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(((withExecutionTime && param0.isTime()) ? "executionTime" : param0.getName()));
    for (int index = 1; index < length; index++) {
      Parameter param = params.get(index);
      builder.append(", ");
      if (withExecutionTime && param.isTime())
        builder.append("executionTime");
      else
        builder.append(param.getName());
    }
    builder.append(")");
    return builder.toString();
  }

  protected String generateParameterSignature(Unit unit) {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    int index = 0;
    for (Parameter param : unit.getParameters()) {
      if (index > 0)
        builder.append(", ");
      builder.append(param.getType());
      builder.append(' ').append(param.getName());
      index++;
    }
    builder.append(')');
    return builder.toString();
  }

  public List<String> generateSource() {
    addPackage();
    addClassComment();
    addDecoratorClass();
    return Collections.unmodifiableList(lines);
  }

  protected Generator inc() {
    indentationDepth++;
    return this;
  }

}
