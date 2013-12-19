package com.github.sormuras.compayler;

import java.util.Collections;
import java.util.List;

/**
 * Feature descriptor tag class.
 * <p>
 * All example javadoc comments below refer to {@link CharSequence#subSequence(int, int)} method.
 * 
 * @author Christian Stein
 */
public class Tag {

  public static List<String> emptyStringList() {
    return Collections.emptyList();
  }

  /**
   * {@code "subSequence"}
   */
  private final String name;

  /**
   * {@code "com.github.sormuras.compayler.generated"}
   */
  private final String packageName;

  /**
   * {@code ["start", "end"]}
   */
  private final List<String> parameterNames;

  /**
   * {@code ["int", "int"]}
   */
  private final List<String> parameterTypes;

  /**
   * {@code "java.lang.CharSequence"}
   */
  private final String returnType;

  /**
   * {@code ["java.io.IOException"]}
   */
  private final List<String> throwing;

  /**
   * {@code true}, if the unit is unique within the source context.
   * 
   * For example: 4-times {@code true} in {@link CharSequence} and 1-time {@code false} in {@link Appendable}
   */
  private final boolean unique;

  /**
   * C'tor.
   */
  public Tag(String name, boolean unique) {
    this(name, "", emptyStringList(), emptyStringList(), void.class.toString(), emptyStringList(), unique);
  }

  /**
   * C'tor.
   */
  public Tag(String name, String packageName, List<String> parameterNames, List<String> parameterTypes, String returnType,
      List<String> throwing, boolean unique) {
    if (name.contains("."))
      throw new IllegalArgumentException("unit name must not contain '.' char(s)");
    this.name = name;
    this.packageName = packageName;
    this.parameterNames = buildUnmodifiableList(parameterNames);
    this.parameterTypes = buildUnmodifiableList(parameterTypes);
    this.returnType = returnType;
    this.throwing = buildUnmodifiableList(throwing);
    this.unique = unique;
  }

  private List<String> buildUnmodifiableList(List<String> strings) {
    List<String> empty = Collections.emptyList();
    return strings.isEmpty() ? empty : Collections.unmodifiableList(strings);
  }

  public String getName() {
    return name;
  }

  public String getPackageName() {
    return packageName;
  }

  public List<String> getParameterNames() {
    return parameterNames;
  }

  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  public String getReturnType() {
    return returnType;
  }

  public List<String> getThrowing() {
    return throwing;
  }

  public boolean isUnique() {
    return unique;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Tag [name=");
    builder.append(name);
    builder.append(", packageName=");
    builder.append(packageName);
    builder.append(", parameterNames=");
    builder.append(parameterNames);
    builder.append(", parameterTypes=");
    builder.append(parameterTypes);
    builder.append(", returnType=");
    builder.append(returnType);
    builder.append(", throwing=");
    builder.append(throwing);
    builder.append(", unique=");
    builder.append(unique);
    builder.append("]");
    return builder.toString();
  }

}
