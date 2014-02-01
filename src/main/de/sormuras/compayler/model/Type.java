package de.sormuras.compayler.model;

import java.util.HashMap;
import java.util.Map;

public class Type {

  private static final Map<String, Type> TYPES = new HashMap<>();

  public static final Type VOID = forName("void");

  public static String brackets(int dimension, boolean variable) {
    if (dimension == 0)
      return "";
    if (dimension == 1)
      return variable ? "..." : "[]";
    if (dimension == 2)
      return variable ? "[]..." : "[][]";
    StringBuilder builder = new StringBuilder();
    for (int i = 1; i <= dimension - 1; i++) {
      builder.append("[]");
    }
    builder.append(variable ? "..." : "[]");
    return builder.toString();
  }

  public static Type forClass(Class<?> classType) {
    int dimension = 0;
    while (classType.isArray()) {
      classType = classType.getComponentType();
      dimension++;
    }
    return forName(classType.getCanonicalName(), dimension);
  }

  /**
   * Simple getter.
   */
  public static Type forName(String name) {
    return forName(name, "", 0);
  }

  /**
   * Array getter.
   */
  public static Type forName(String name, int dimension) {
    return forName(name, "", dimension);
  }

  /**
   * Parameterized getter with actual type arguments.
   */
  public static Type forName(String name, String typeargs) {
    return forName(name, typeargs, 0);
  }

  private static Type forName(String name, String typeargs, int dimension) {
    String key = name + typeargs + dimension;
    Type type = TYPES.get(key);
    if (type == null) {
      type = new Type(name, typeargs, dimension);
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

  private final String binaryName;
  private final int dimension;
  private final String name;
  private final String typeargs;
  private final String wrapped;

  private Type(String binaryName, String typeargs, int dimension) {
    this.binaryName = binaryName;
    this.name = binaryName.replace('$', '.');
    this.typeargs = typeargs;
    this.dimension = dimension;
    this.wrapped = wrap(binaryName);
  }

  public String getBinaryName() {
    return binaryName;
  }

  public int getDimension() {
    return dimension;
  }

  public String getName() {
    return name;
  }

  public String getTypeArgs() {
    return typeargs;
  }

  public String getWrapped() {
    return wrapped;
  }

  public boolean isArray() {
    return dimension > 0;
  }

  public boolean isGeneric() {
    return !typeargs.isEmpty();
  }

  public boolean isPrimitive() {
    return !name.equals(wrapped);
  }

  public boolean isVoid() {
    return "void".equals(name);
  }

  @Override
  public String toString() {
    return toString(false);
  }

  public String toString(boolean variable) {
    return name + typeargs + brackets(dimension, variable);
  }

}
