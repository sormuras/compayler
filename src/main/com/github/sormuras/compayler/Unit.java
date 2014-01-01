package com.github.sormuras.compayler;

/**
 * Executable class implementation feature descriptor bean unit.
 * 
 * @author Christian Stein
 */
public class Unit {

  private String className;
  private String packageName;
  private String returnType;
  private long serialVersionUID;
  private final Tag tag;

  public Unit(Tag tag) {
    this.tag = tag;
    setPackageName(tag.getPackageName());
    setSerialVersionUID(0L);
  }

  public String getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getParameterName(int index) {
    if (index < 0 )
      return "-1-";
    return tag.getParameterNames().get(index);
  }

  /**
   * <pre>
   * 0 = "()"
   * 1 = "(p0)"
   * 2 = "(p0, p1)"
   * n = "(p0, ..., pn-1)"
   * </pre>
   * 
   * @return {@code "(p0, p1, ... pn-1)"}
   */
  public String getParameterParentheses() {
    int length = tag.getParameterTypes().size();
    if (length == 0)
      return "()";
    if (length == 1)
      return "(" + getParameterName(0) + ")";
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(getParameterName(0));
    for (int i = 1; i < length; i++) {
      builder.append(", ").append(getParameterName(i));
    }
    builder.append(")");
    return builder.toString();
  }

  /**
   * @return {@code "(p0, executionTime, ... pn-1)"}
   */
  public String getParameterParenthesesWithExecutionTime() {
    return getParameterParentheses().replace(getParameterName(tag.getPrevalentTime()), "executionTime");
  }

  public String getParameterSignature() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    int index = 0;
    for (String type : tag.getParameterTypes()) {
      if (index > 0)
        builder.append(", ");
      builder.append(type).append(' ').append(tag.getParameterNames().get(index));
      index++;
    }
    builder.append(')');
    return builder.toString();
  }

  public String getReturnType() {
    return returnType;
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }

  public Tag getTag() {
    return tag;
  }

  public boolean hasParameters() {
    return !tag.getParameterTypes().isEmpty();
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

}
