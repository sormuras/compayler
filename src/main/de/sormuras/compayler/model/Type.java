package de.sormuras.compayler.model;

public class Type {

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

  public static String canonical(String binaryName) {
    String canonical = binaryName.replace('$', '.');
    // array?
    if (canonical.startsWith("[")) {
      int dimension = canonical.length() - canonical.replace("[", "").length();
      if (canonical.endsWith(";")) {
        canonical = canonical.substring(dimension + 1, canonical.length() - 1);
      } else {
        canonical = element(canonical.charAt(canonical.length() - 1)).getCanonicalName();
      }
      canonical = canonical + brackets(dimension, false);
    }
    return canonical;
  }

  public static int dimension(Class<?> classType) {
    int dimension = 0;
    while (classType.isArray()) {
      classType = classType.getComponentType();
      dimension++;
    }
    return dimension;
  }

  public static Class<?> element(char encoding) {
    switch (encoding) {
    case 'Z':
      return boolean.class;
    case 'B':
      return byte.class;
    case 'C':
      return char.class;
    case 'D':
      return double.class;
    case 'F':
      return float.class;
    case 'I':
      return int.class;
    case 'J':
      return long.class;
    case 'S':
      return short.class;
    }
    throw new IllegalArgumentException("Unsupported encoding: " + encoding);
  }

  public static String simple(String binaryName) {
    int i = binaryName.lastIndexOf('$');
    if (i < 0)
      i = binaryName.lastIndexOf('.');
    if (i >= 0)
      return binaryName.substring(i);
    return binaryName;
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
  private final String canonicalName;
  private final String simpleName;
  private final String typeParameterSuffix;
  private final String wrappedName;

  public Type(Class<?> type) {
    this(type, "");
  }

  public Type(Class<?> type, String typeParameterSuffix) {
    this(type.getName(), type.getCanonicalName(), type.getSimpleName(), typeParameterSuffix);
    assert this.isArray() == type.isArray();
    assert this.isPrimitive() == type.isPrimitive();
    assert this.isVoid() == (type == void.class || type == Void.class);
    assert this.getArrayDimension() == dimension(type);
  }

  /**
   * The binary name of the type as returned by {@link Class#getName()}.
   * 
   * @param binaryName
   */
  public Type(String binaryName) {
    this(binaryName, "");
  }

  public Type(String binaryName, String typeParameterSuffix) {
    this(binaryName, canonical(binaryName), simple(binaryName), typeParameterSuffix);
  }

  public Type(String binaryName, String canonicalName, String simpleName, String typeParameterSuffix) {
    this.binaryName = binaryName;
    this.canonicalName = canonicalName;
    this.simpleName = simpleName;
    this.typeParameterSuffix = typeParameterSuffix;
    this.wrappedName = wrap(getBinaryName());
  }

  public int getArrayDimension() {
    return binaryName.length() - binaryName.replace("[", "").length();
  }

  public Type getArrayType() {
    if (!isArray())
      throw new IllegalStateException(binaryName + " isn't an array!");
    // primitive element type
    String elementType = binaryName.substring(getArrayDimension());
    if (elementType.length() == 1) {
      return new Type(element(elementType.charAt(0)));
    }
    // class or interface element type
    assert elementType.startsWith("L") && elementType.endsWith(";");
    elementType = elementType.substring(1, elementType.length() - 1);
    try {
      return new Type(Class.forName(elementType));
    } catch (ClassNotFoundException e) {
      // ignore and fall back to string manipulation magic
    }
    return new Type(elementType);
  }

  public String getBinaryName() {
    return binaryName;
  }

  public String getCanonicalName() {
    return canonicalName;
  }

  public String getPackageName() {
    int lastDot = binaryName.lastIndexOf('.');
    if (lastDot < 0)
      return "";
    return binaryName.substring(0, lastDot);
  }

  public String getSimpleName() {
    return simpleName;
  }

  public String getTypeParameterSuffix() {
    return typeParameterSuffix;
  }

  public String getWrappedName() {
    return wrappedName;
  }

  public boolean isArray() {
    return binaryName.startsWith("[");
  }

  public boolean isPrimitive() {
    return !binaryName.equals(wrappedName);
  }

  public boolean isVoid() {
    return binaryName.equals("void") || binaryName.equals("java.lang.Void");
  }

  @Override
  public String toString() {
    return toString(false);
  }

  public String toString(boolean variable) {
    if (variable && isArray()) {
      return getArrayType() + brackets(getArrayDimension(), true);
    }
    return canonicalName + typeParameterSuffix;
  }

}
