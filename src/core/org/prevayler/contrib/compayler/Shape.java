package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Util.merge;

import java.util.Collections;
import java.util.List;

import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Util;

/**
 * Method signature class.
 * 
 * @author Christian Stein
 */
public class Shape {

  private final String name;
  private final List<Param> params;
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

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Shape other = (Shape) obj;
    if (!name.equals(other.name))
      return false;
    if (!params.equals(other.params))
      return false;
    if (!returnType.equals(other.returnType))
      return false;
    return true;
  }

  public String getName() {
    return name;
  }

  public List<Param> getParams() {
    return params;
  }

  public Type getReturnType() {
    return returnType;
  }

  public List<Type> getThrowables() {
    return throwables;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + params.hashCode();
    result = prime * result + returnType.hashCode();
    return result;
  }

  public boolean isUnique() {
    return unique;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("public");
    builder.append(" ");
    builder.append(getReturnType().getCanonicalName());
    builder.append(" ");
    builder.append(getName());
    builder.append(getParams().isEmpty() ? "()" : merge("(", ")", ", ", getParams()));
    if (getThrowables().isEmpty())
      return builder.toString();
    builder.append(" throws ");
    builder.append(Util.merge("", "", ", ", getThrowables().toArray()));
    return builder.toString();
  }

  private <T> List<T> unmodifiable(List<T> list) {
    List<T> empty = Collections.emptyList();
    return (list == null || list.isEmpty()) ? empty : Collections.unmodifiableList(list);
  }

}
