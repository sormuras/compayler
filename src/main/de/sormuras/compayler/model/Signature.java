package de.sormuras.compayler.model;

import java.util.Collections;
import java.util.List;

public class Signature {

  private final List<Field> fields;
  private final String name;
  private final Type returnType;
  private final List<Type> throwables;
  private final boolean unique;

  public Signature(String name, Type returnType, List<Field> fields, List<Type> throwables, boolean unique) {
    if (name == null || returnType == null)
      throw new IllegalArgumentException("name and returnType must not be null");
    this.name = name;
    this.returnType = returnType;
    this.fields = unmodifiable(fields);
    this.throwables = unmodifiable(throwables);
    this.unique = unique;
  }

  public List<Field> getFields() {
    return fields;
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

}