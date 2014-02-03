package de.sormuras.compayler.model;

public class Field {

  private int index;
  private String name = "";
  private boolean time = false;
  private Type type = null;
  private boolean variable;

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
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

  public void setType(Type type) {
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