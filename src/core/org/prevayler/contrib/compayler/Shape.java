package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Util.unmodifiable;

import java.util.List;

/**
 * Method signature shape bean class.
 * 
 * @author Christian Stein
 */
public class Shape implements Unit {

  private Mode mode;
  private final String name;
  private final List<Param> params;
  private Long serialVersionUID;
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
    setMode(Mode.TRANSACTION);
    setSerialVersionUID(null);
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

  @Override
  public Mode getMode() {
    return mode;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Param> getParams() {
    return params;
  }

  @Override
  public Long getSerialVersionUID() {
    return serialVersionUID;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
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

  @Override
  public boolean isUnique() {
    return unique;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public void setSerialVersionUID(Long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

}
