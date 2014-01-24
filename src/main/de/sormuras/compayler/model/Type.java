package de.sormuras.compayler.model;

import java.util.HashMap;
import java.util.Map;

public class Type {

  private static final Map<String, Type> TYPES = new HashMap<>();

  public static final Type VOID = forName("void");

  public static Type forName(String name) {
    return forName(name, 0);
  }

  public static Type forName(String name, int dimension) {
    String key = name + dimension;
    Type type = TYPES.get(key);
    if (type == null) {
      type = new Type(name, dimension);
      TYPES.put(key, type);
    }
    return type;
  }

  public static String wrap(String name) {
    switch (name) {
    case "boolean":
      return "java.lang.Boolean";
    case "byte":
      return "java.lang.Byte";
    case "char":
      return "java.lang.Character";
    case "double":
      return "java.lang.Double";
    case "float":
      return "java.lang.Float";
    case "int":
      return "java.lang.Integer";
    case "long":
      return "java.lang.Long";
    case "short":
      return "java.lang.Short";
    case "void":
      return "java.lang.Void";
    }
    return name;
  }

  private final int dimension;
  private final String name;
  private final String wrapped;

  private Type(String name, int dimension) {
    this.name = name;
    this.dimension = dimension;
    this.wrapped = wrap(name);
  }

  public int getDimension() {
    return dimension;
  }

  public String getName() {
    return name;
  }

  public String getWrapped() {
    return wrapped;
  }

  public boolean isArray() {
    return dimension > 0;
  }

  public boolean isPrimitive() {
    return !name.equals(wrapped);
  }

}
