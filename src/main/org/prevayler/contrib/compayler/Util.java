package org.prevayler.contrib.compayler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Static utilities.
 * 
 * @author Christian Stein
 */
public final class Util {

  /**
   * Returns the smallest index of the annotation class with the method parameter list.
   * 
   * @param method
   *          to analyze
   * @param annotationType
   *          to look for
   * @return the zero-based index of the annotation within the parameter list, -1 if annotationType is not present
   */
  public static int index(Method method, Class<? extends Annotation> annotationType) {
    Annotation[][] annotationsArray = method.getParameterAnnotations();
    for (int index = 0; index < annotationsArray.length; index++) {
      Annotation[] annotations = annotationsArray[index];
      for (int offset = 0; offset < annotations.length; offset++) {
        if (annotationType == annotations[offset].annotationType()) {
          return index;
        }
      }
    }
    return -1;
  }

  /**
   * Returns the canonical name of the class or, if the class belongs to "java.lang" package, the simple class name.
   * 
   * @param type
   *          the type to give the name for
   * @return the "java source" name of the type
   */
  public static String name(Class<?> type) {
    Package pack = type.getPackage();
    if (type.isArray())
      pack = type.getComponentType().getPackage();
    String packageName = null;
    if (pack != null)
      packageName = pack.getName();
    if ("java.lang".equals(packageName))
      return type.getSimpleName();
    return type.getCanonicalName();
  }

  /**
   * Returns wrapper class for the primitive type.
   * 
   * @param type
   *          the type to wrap
   * @return if primitive type return its wrapper class, else type
   */
  public static Class<?> wrap(Class<?> type) {
    // if primitive type return its wrapper class
    if (type == boolean.class)
      return Boolean.class;
    if (type == byte.class)
      return Byte.class;
    if (type == char.class)
      return Character.class;
    if (type == double.class)
      return Double.class;
    if (type == float.class)
      return Float.class;
    if (type == int.class)
      return Integer.class;
    if (type == long.class)
      return Long.class;
    if (type == short.class)
      return Short.class;
    if (type == void.class)
      return Void.class;
    // no primitive, no wrap
    return type;
  }

}
