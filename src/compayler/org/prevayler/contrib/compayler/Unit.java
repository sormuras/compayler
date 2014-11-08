package org.prevayler.contrib.compayler;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prevayler.contrib.compayler.Compayler.ExecutionMode;

public class Unit {

  public class Parameter {

    private boolean last;
    private String name;
    private boolean time;
    private String type;
    private boolean vars;

    public String getName() {
      return name;
    }

    public String getType() {
      return type;
    }

    public boolean isLast() {
      return last;
    }

    public boolean isTime() {
      return time;
    }

    public boolean isVars() {
      return vars;
    }

    public void setLast(boolean last) {
      this.last = last;
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

    public void setVars(boolean vars) {
      this.vars = vars;

    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(type);
      if (isVars()) {
        // assert type.isArray() : type.toString();
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

  private static final SecureRandom random = new SecureRandom();

  public static void sort(List<Unit> units) {
    Collections.sort(units, (u1, u2) -> u1.toString().compareTo(u2.toString()));
  }

  public static void updateAllUniqueProperties(List<Unit> units) {
    Map<String, Boolean> map = new HashMap<>();
    units.forEach(unit -> {
      Boolean old = map.put(unit.getName(), Boolean.TRUE);
      if (old == null)
        return;
      map.put(unit.getName(), Boolean.FALSE);
    });
    units.forEach(unit -> unit.setUnique(map.get(unit.getName())));
  }

  private boolean chainable;
  private boolean defaults;
  private final long id = random.nextLong();
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

  public long getId() {
    return id;
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
    builder.append(", id=0x");
    builder.append(Long.toHexString(id));
    builder.append("]");
    return builder.toString();
  }

}
