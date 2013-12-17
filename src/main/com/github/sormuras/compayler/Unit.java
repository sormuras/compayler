package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.List;

/**
 * Class feature descriptor unit.
 * <p>
 * All example javadoc comments below refer to {@link CharSequence#subSequence(int, int)} method.
 * 
 * @author Christian Stein
 */
public class Unit {

  /**
   * {@code "SubSequenceQuery"}
   */
  public final String className;

  /**
   * {@code "com.github.sormuras.compayler.generated"}
   */
  public String packageName;

  /**
   * {@code ["start", "end"]}
   */
  public List<String> parameterNames = new ArrayList<>();

  /**
   * {@code ["int", "int"]}
   */
  public List<String> parameterTypes = new ArrayList<>();

  /**
   * {@code "java.lang.CharSequence"}
   */
  public String returnType = void.class.toString();

  /**
   * {@code ["java.io.IOException"]}
   */
  public List<String> throwing = new ArrayList<>();

  public Unit(String className) {
    if (className.contains("."))
      throw new IllegalArgumentException("className must not contain '.' chars");
    this.className = className;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Unit [className=");
    builder.append(className);
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
    builder.append("]");
    return builder.toString();
  }

}
