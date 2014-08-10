package org.prevayler.contrib.compayler;

import java.util.ArrayList;
import java.util.List;

import org.prevayler.contrib.compayler.Compayler.ExecutionMode;

public class Unit {

  public class Parameter {

    private String name;
    private boolean time;
    private String type;

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public boolean isTime() {
      return time;
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

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(type);
      builder.append(" ");
      builder.append(name);
      return builder.toString();
    }

  }

  private boolean chainable;
  private boolean defaults;
  private ExecutionMode mode = ExecutionMode.TRANSACTION;
  private String name;
  private List<Parameter> parameters = new ArrayList<>();
  private String returns;
  private Long serialVersionUID = Long.valueOf(0L);
  private List<String> throwns = new ArrayList<>();
  private boolean unique;
  private boolean varargs;

  public Parameter createParameter() {
    Parameter parameter = new Parameter();
    parameters.add(parameter);
    return parameter;
  }

  public ExecutionMode getMode() {
    return mode;
  }

  public String getName() {
    return name;
  }

  public List<Parameter> getParameters() {
    return parameters;
  }

  public String getReturns() {
    return returns;
  }

  public Long getSerialVersionUID() {
    return serialVersionUID;
  }

  public List<String> getThrowns() {
    return throwns;
  }

  public boolean isChainable() {
    return chainable;
  }

  public boolean isDefaults() {
    return defaults;
  }

  public boolean isUnique() {
    return unique;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public void setChainable(boolean chainable) {
    this.chainable = chainable;
  }

  public void setDefaults(boolean defaults) {
    this.defaults = defaults;
  }

  public void setMode(ExecutionMode mode) {
    this.mode = mode;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setReturns(String returns) {
    this.returns = returns;
  }

  public void setSerialVersionUID(Long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public void setVarargs(boolean varargs) {
    this.varargs = varargs;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Unit [name=");
    builder.append(name);
    builder.append(", parameters=");
    builder.append(parameters);
    builder.append(", returns=");
    builder.append(returns);
    builder.append(", mode=");
    builder.append(mode);
    builder.append(", chainable=");
    builder.append(chainable);
    builder.append(", defaults=");
    builder.append(defaults);
    builder.append(", serialVersionUID=");
    builder.append(serialVersionUID);
    builder.append(", throwns=");
    builder.append(throwns);
    builder.append(", unique=");
    builder.append(unique);
    builder.append(", varargs=");
    builder.append(varargs);
    builder.append("]");
    return builder.toString();
  }

}
