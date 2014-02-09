package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Util.canonical;
import static org.prevayler.contrib.compayler.Util.element;
import static org.prevayler.contrib.compayler.Util.packaged;
import static org.prevayler.contrib.compayler.Util.simple;
import static org.prevayler.contrib.compayler.Util.wrap;

import java.util.Objects;

public class Type {

  private final String binaryName;
  private final String canonicalName;
  private final String simpleName;
  private final Type superType;

  public Type(Class<?> type) {
    this(type.getName(), type.getCanonicalName(), type.getSimpleName(), type.getSuperclass() != null ? new Type(type.getSuperclass())
        : null);
    assert type.getCanonicalName().equals(canonical(binaryName));
    assert type.getSimpleName().equals(simple(canonical(binaryName)));
    assert type.isArray() == isArray();
    assert type.isPrimitive() == isPrimitive();
    assert type == void.class || type == Void.class == isVoid();
  }

  public Type(String binaryName) {
    this(binaryName, canonical(binaryName), simple(canonical(binaryName)), new Type(Object.class));
  }

  public Type(String binaryName, String canonicalName, String simpleName, Type superType) {
    Objects.requireNonNull(binaryName);
    this.binaryName = binaryName;
    this.canonicalName = canonicalName;
    this.simpleName = simpleName;
    this.superType = superType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return binaryName.equals(((Type) obj).binaryName);
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

  /**
   * Returns the name of the entity (class, interface, array class, primitive type, or void) represented by this Class object, as a String.
   * 
   * @return the name of the class or interface represented by this object
   * @see Class#getName()
   */
  public String getBinaryName() {
    return binaryName;
  }

  /**
   * Returns the canonical name of the underlying class as defined by the Java Language Specification.
   * 
   * @return the canonical name
   * @see Class#getCanonicalName()
   */
  public String getCanonicalName() {
    return canonicalName;
  }

  /**
   * @return same as {@link #getCanonicalName()}, but wrapper class type for primitives
   */
  public String getCanonicalNameWrapped() {
    return isPrimitive() ? wrap(canonicalName).getCanonicalName() : canonicalName;
  }

  /**
   * Returns package name as string
   * 
   * @return the package name.
   */
  public String getPackageName() {
    return packaged(binaryName);
  }

  /**
   * Returns the simple name of the underlying class as given in the source code.
   * 
   * @return the simple name
   * @see Class#getSimpleName()
   */
  public String getSimpleName() {
    return simpleName;
  }

  /**
   * Returns the simple name of the underlying class as given in the source code.
   * 
   * @return the supertype of the type represented by this object.
   * @see Class#getSuperclass()
   */
  public Type getSuperType() {
    return superType;
  }

  @Override
  public int hashCode() {
    return binaryName.hashCode();
  }

  /**
   * @return true if this object represents an array class; false otherwise.
   * @see Class#isArray()
   */
  public boolean isArray() {
    return binaryName.startsWith("[");
  }

  /**
   * @return true if this object represents a simple data type; false otherwise.
   * @see Class#isPrimitive()
   */
  public boolean isPrimitive() {
    return binaryName.equals("boolean") || binaryName.equals("byte") || binaryName.equals("char") || binaryName.equals("double")
        || binaryName.equals("float") || binaryName.equals("int") || binaryName.equals("long") || binaryName.equals("short")
        || binaryName.equals("void");
  }

  /**
   * @return true if this object represents the void/Void data type; false otherwise.
   */
  public boolean isVoid() {
    return binaryName.equals("void") || binaryName.equals("java.lang.Void");
  }

}
