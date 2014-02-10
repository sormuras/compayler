package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Util.merge;

import java.util.Collections;
import java.util.List;

import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Util;

public class Shape {

  private final List<Param> params;
  private final String name;
  private final Type returnType;
  private final List<Type> throwables;
  private final boolean unique;

  public Shape(String name, Type returnType, List<Param> fields, List<Type> throwables, boolean unique) {
    if (name == null || returnType == null)
      throw new IllegalArgumentException("name and returnType must not be null");
    this.name = name;
    this.returnType = returnType;
    this.params = unmodifiable(fields);
    this.throwables = unmodifiable(throwables);
    this.unique = unique;
  }

  public List<Param> getParams() {
    return params;
  }

  public String getName() {
    return name;
  }

  public Type getReturnType() {
    return returnType;
  }

  public List<Type> getThrowables() {
    return throwables;
  }

  public boolean isUnique() {
    return unique;
  }

  private <T> List<T> unmodifiable(List<T> list) {
    List<T> empty = Collections.emptyList();
    return (list == null || list.isEmpty()) ? empty : Collections.unmodifiableList(list);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("public").append(" ");
    builder.append(getReturnType().getCanonicalName()).append(" ");
    builder.append(getName());
    builder.append(getParams().isEmpty() ? "()" : merge("(", ")", ", ", getParams()));
    if (!getThrowables().isEmpty()) {
      builder.append(" throws ");
      builder.append(Util.merge("", "", ", ", getThrowables().toArray()));
    }
    return builder.toString();
  }

}
