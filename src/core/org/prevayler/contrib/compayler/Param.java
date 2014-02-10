package org.prevayler.contrib.compayler;

import org.prevayler.contrib.compayler.Type;

public class Param {

  private int index;
  private String name = "";
  private boolean time = false;
  private Type type = null;
  private boolean variable;
  
  public Param() {
    this("", null, false);
  }
  
  public Param(String name, Type type) {
    this(name, type, false);
  }
  
  public Param(String name, Type type, boolean variable) {
    setName(name);
    setType(type);
    setVariable(variable);
  }

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
    builder.append(type);
    if (variable) {
      assert type.isArray();
      assert builder.charAt(builder.length() - 2) == '[';
      assert builder.charAt(builder.length() - 1) == ']';
      builder.setLength(builder.length() - 2); // remove "[]" from end of the builder buffer
      builder.append("...");
    }
    builder.append(" ");
    builder.append(name);
    return builder.toString();
  }

}
