package com.github.sormuras.compayler;

import java.util.List;
import java.util.zip.CRC32;

import com.github.sormuras.compayler.Compayler.Configuration;
import com.github.sormuras.compayler.Compayler.Kind;
import com.github.sormuras.compayler.Compayler.Mode;

public class Description {

  public static class Field {

    private int index;
    private String name = "";
    private boolean time = false;
    private String type = "";
    private boolean variable;

    public int getIndex() {
      return index;
    }

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public boolean isTime() {
      return time;
    }

    public boolean isVariable() {
      return variable;
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setTime(boolean time) {
      this.time = time;
    }

    public void setType(String type) {
      this.type = type;
    }

    public void setVariable(boolean variable) {
      this.variable = variable;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("");
      builder.append(type);
      if (variable)
        builder.append("...");
      builder.append(" ");
      builder.append(name);
      builder.append("");
      return builder.toString();
    }

  }

  public static class Signature {

    private final List<Field> fields;
    private final String name;
    private final String returnType;
    private final List<String> throwables;
    private final boolean unique;

    public Signature(String name, String returnType, List<Field> fields, List<String> throwables, boolean unique) {
      this.name = name;
      this.returnType = returnType;
      this.fields = Compayler.unmodifiableList(fields);
      this.throwables = Compayler.unmodifiableList(throwables);
      this.unique = unique;
    }

  }

  public class Variable {

    private Mode mode = Mode.TRANSACTION;
    private long serialVersionUID = 0L;

    public Mode getMode() {
      return mode;
    }

    public long getSerialVersionUID() {
      return serialVersionUID;
    }

    public void setMode(Mode mode) {
      this.mode = mode;
    }

    public void setSerialVersionUID(long serialVersionUID) {
      this.serialVersionUID = serialVersionUID;
    }
  }

  private final Configuration configuration;
  private final Signature signature;
  private final Variable variable;

  public Description(Configuration configuration, Signature signature) {
    this.configuration = configuration;
    this.signature = signature;
    this.variable = new Variable();
  }

  protected String getChecksum() {
    CRC32 crc32 = configuration.getChecksumBuilder();
    crc32.reset();
    for (Field field : signature.fields) {
      crc32.update(field.getType().getBytes());
    }
    return Long.toString(crc32.getValue(), Character.MAX_RADIX).toUpperCase();
  }

  public String getClassName() {
    // capitalize name
    String name = getName();
    name = name.toUpperCase().charAt(0) + name.substring(1);
    // done, if name is unique
    if (isNameUnique()) {
      return name;
    }
    // name is overloaded, append hash for parameter type name strings
    return name + getChecksum();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  protected String getExceptions(String separator) {
    StringBuilder builder = new StringBuilder();
    boolean comma = false;
    for (String throwable : getThrowables()) {
      if (comma) {
        builder.append(separator);
      }
      builder.append(" ");
      builder.append(throwable);
      comma = true;
    }
    return builder.toString().trim();
  }

  public List<Field> getFields() {
    return signature.fields;
  }

  protected String getImplements() {
    StringBuilder builder = configuration.getStringBuilder();
    Kind kind = getKind();
    builder.append(kind.getExecutableInterface().getCanonicalName());
    builder.append('<');
    builder.append(configuration.getInterfaceName());
    if (kind != Kind.TRANSACTION) {
      assert getReturnType().equals("void");
      builder.append(',').append(' ').append(Compayler.wrap(getReturnType()));
    }
    builder.append('>');
    return builder.toString();
  }

  protected Kind getKind() {
    if (getMode() == Mode.QUERY)
      return Kind.QUERY;
    if (getReturnType().equals("void"))
      return Kind.TRANSACTION;
    if (getThrowables().isEmpty())
      return Kind.TRANSACTION_QUERY;
    // fallen through
    return Kind.TRANSACTION_QUERY_EXCEPTION;
  }

  protected String getMethodDeclaration() {
    StringBuilder builder = new StringBuilder();
    builder.append(getReturnType());
    builder.append(" ");
    builder.append(getName());
    builder.append(getParameterSignature());
    if (getThrowables().isEmpty())
      return builder.toString();

    builder.append(" throws ");
    builder.append(getExceptions(","));
    return builder.toString();
  }

  public Mode getMode() {
    return variable.mode;
  }

  public String getName() {
    return signature.name;
  }

  protected String getParameterParentheses() {
    int length = getFields().size();
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + getFields().get(0).getName() + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(getFields().get(0).getName());
    for (int index = 1; index < length; index++) {
      builder.append(", ").append(getFields().get(index).getName());
    }
    builder.append(")");
    return builder.toString();
  }

  protected String getParameterParenthesesWithExecutionTime(Scribe scribe) {
    String parantheses = getParameterParentheses();
    for (Field field : getFields()) {
      if (field.isTime())
        return parantheses.replace(field.getName(), "executionTime");
    }
    return parantheses;
  }

  protected String getParameterSignature() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (Field field : getFields()) {
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

  public String getReturnType() {
    return signature.returnType;
  }

  public long getSerialVersionUID() {
    return variable.serialVersionUID;
  }

  public Signature getSignature() {
    return signature;
  }

  public List<String> getThrowables() {
    return signature.throwables;
  }

  public Variable getVariable() {
    return variable;
  }

  public boolean isNameUnique() {
    return signature.unique;
  }

  @Override
  public String toString() {
    StringBuilder builder = configuration.getStringBuilder();
    builder.append("Description [");
    builder.append("name=").append(getName());
    builder.append(", checksum=").append(getChecksum());
    builder.append(", className=").append(getClassName());
    builder.append(", returnType=").append(getReturnType());
    builder.append(", mode=").append(getMode());
    builder.append(", unique=").append(isNameUnique());
    builder.append(", fields=").append(getFields());
    builder.append(", throwables=").append(getThrowables());
    builder.append("]");
    return builder.toString();
  }

}
