package org.prevayler.contrib.compayler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods.
 * 
 * @author Christian Stein
 */
public class Util {

  public static Object[] array(Iterable<?> elements) {
    final ArrayList<Object> list = new ArrayList<>();
    for (Object element : elements)
      list.add(element);
    return list.toArray(new Object[list.size()]);
  }

  public static String brackets(int dimension, boolean variable) {
    if (dimension < 0)
      throw new IllegalArgumentException("dimension must not be negative! [dimension=" + dimension + "]");
    if (dimension == 0)
      return "";
    if (dimension == 1)
      return variable ? "..." : "[]";
    if (dimension == 2)
      return variable ? "[]..." : "[][]";
    StringBuilder builder = new StringBuilder("[][]");
    for (int i = 3; i < dimension; i++) {
      builder.append("[]");
    }
    builder.append(variable ? "..." : "[]");
    return builder.toString();
  }

  public static String canonical(String binaryName) {
    String canonical = binaryName.replace('$', '.');
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
    throw new IllegalArgumentException("Illegal encoding: " + encoding);
  }

  public static String merge(String head, String tail, String separator, Iterable<?> iterable) {
    return merge(head, tail, separator, array(iterable));
  }

  public static String merge(String head, String tail, String separator, Iterable<?>... iterables) {
    if (iterables.length == 0)
      return "";
    int count = 0;
    StringBuilder builder = new StringBuilder();
    builder.append(head);
    for (Iterable<?> iterable : iterables) {
      if (count > 0)
        builder.append(separator);
      merge(builder, "", "", separator, array(iterable));
      count++;
    }
    builder.append(tail);
    return builder.toString();
  }

  public static String merge(String head, String tail, String separator, Object... objects) {
    if (objects.length == 0)
      return "";
    if (objects.length == 1)
      return head + objects[0] + tail;
    if (objects.length == 2)
      return head + objects[0] + separator + objects[1] + tail;
    return merge(new StringBuilder(), head, tail, separator, objects).toString();
  }

  public static StringBuilder merge(StringBuilder builder, String head, String tail, String separator, Object... objects) {
    int count = 0;
    builder.append(head);
    for (Object object : objects) {
      if (count > 0)
        builder.append(separator);
      builder.append(object);
      count++;
    }
    builder.append(tail);
    return builder;
  }

  public static String packaged(String binaryName) {
    int lastDot = binaryName.lastIndexOf('.');
    if (lastDot <= 0)
      return "";
    return binaryName.substring(0, lastDot);
  }

  public static String simple(String canoncicalName) {
    if (canoncicalName.startsWith("[") || canoncicalName.contains("$"))
      throw new IllegalArgumentException(canoncicalName + " isn't canonical!");
    int i = canoncicalName.lastIndexOf('.');
    return i < 0 ? canoncicalName : canoncicalName.substring(i + 1);
  }

  public static <T> List<T> unmodifiable(List<T> list) {
    List<T> empty = Collections.emptyList();
    return (list == null || list.isEmpty()) ? empty : Collections.unmodifiableList(list);
  }

  public static Class<?> wrap(String name) {
    switch (name) {
    case "boolean":
      return Boolean.class;
    case "byte":
      return Byte.class;
    case "char":
      return Character.class;
    case "double":
      return Double.class;
    case "float":
      return Float.class;
    case "int":
      return Integer.class;
    case "long":
      return Long.class;
    case "short":
      return Short.class;
    case "void":
      return Void.class;
    }
    throw new IllegalArgumentException(name + " isn't a primitive type!");
  }

  private Util() {
    // hidden
  }

}
