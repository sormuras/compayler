package sandbox;

import java.util.List;

import sandbox.Compayler.Kind;
import sandbox.Compayler.Mode;

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

  private final long checksum;
  private String className;
  private final List<Field> fields;
  private Kind kind;
  private String methodDeclaration;
  private Mode mode;
  private final String name;
  private boolean nameUnique;
  private String parameterParentheses;
  private final String returnType;
  private long serialVersionUID;
  private final List<String> throwables;

  public Description(long checksum, String name, String returnType, List<Field> fields, List<String> throwables) {
    this.checksum = checksum;
    this.name = name;
    this.returnType = returnType;
    this.fields = Compayler.unmodifiableList(fields);
    this.throwables = Compayler.unmodifiableList(throwables);
    //
    this.className = "";
    this.kind = Kind.TRANSACTION;
    this.mode = Mode.TRANSACTION;
    this.nameUnique = true;
    this.methodDeclaration = "";
    this.parameterParentheses = "";
    this.setSerialVersionUID(0L);
  }

  public Description(String name, String returnType) {
    this(0L, name, returnType, null, null);
  }

  public long getChecksum() {
    return checksum;
  }

  public String getChecksum(int radix) {
    return Long.toString(checksum, radix).toUpperCase();
  }

  public String getClassName() {
    return className;
  }

  public List<Field> getFields() {
    return fields;
  }

  public Kind getKind() {
    return kind;
  }

  public String getMethodDeclaration() {
    return methodDeclaration;
  }

  public Mode getMode() {
    return mode;
  }

  public String getName() {
    return name;
  }

  public String getParameterParentheses() {
    return parameterParentheses;
  }

  public String getReturnType() {
    return returnType;
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }

  public List<String> getThrowables() {
    return throwables;
  }

  public boolean isNameUnique() {
    return nameUnique;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }

  public void setMethodDeclaration(String methodDeclaration) {
    this.methodDeclaration = methodDeclaration;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public void setNameUnique(boolean nameUnique) {
    this.nameUnique = nameUnique;
  }

  public void setParameterParentheses(String parameterParentheses) {
    this.parameterParentheses = parameterParentheses;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Description [");
    builder.append("name=");
    builder.append(name);
    builder.append(", checksum=");
    builder.append(getChecksum(Character.MAX_RADIX));
    builder.append(", className=");
    builder.append(className);
    builder.append(", returnType=");
    builder.append(returnType);
    builder.append(", mode=");
    builder.append(mode);
    builder.append(", unique=");
    builder.append(nameUnique);
    builder.append(", fields=");
    builder.append(fields);
    builder.append(", throwables=");
    builder.append(throwables);
    builder.append("]");
    return builder.toString();
  }

}
