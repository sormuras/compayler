package com.github.sormuras.compayler;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

public class Compayler {

  public static class Configuration {

    private final String decoratorName;
    private final String interfaceName;
    private final String targetPackage;

    public Configuration(String interfaceName) {
      this(interfaceName, simple(interfaceName).toLowerCase(), simple(interfaceName) + "Decorator");
    }

    public Configuration(String interfaceName, String targetPackage, String decoratorName) {
      this.interfaceName = interfaceName;
      this.targetPackage = targetPackage;
      this.decoratorName = decoratorName;
    }

    public String getDecoratorName() {
      return decoratorName;
    }

    public String getInterfaceName() {
      return interfaceName;
    }

    public String getTargetPackage() {
      return targetPackage;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Configuration [interfaceName=");
      builder.append(interfaceName);
      builder.append(", targetPackage=");
      builder.append(targetPackage);
      builder.append(", decoratorName=");
      builder.append(decoratorName);
      builder.append("]");
      return builder.toString();
    }

  }

  public static interface DescriptionFactory {

    List<Description> createDescriptions();

  }

  public static interface DescriptionVisitor {

    void visitDescriptions(List<Description> descriptions);

  }

  public static interface DescriptionWriter {

    Source writeDecorator(List<Description> descriptions);

    Source writeExecutable(Description description);

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Directive {

    Mode value() default Mode.TRANSACTION;

  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public static @interface ExecutionTime {
    // empty
  }

  public static enum Kind {

    QUERY(Query.class, true),

    TRANSACTION(Transaction.class, false),

    TRANSACTION_QUERY(SureTransactionWithQuery.class, false),

    TRANSACTION_QUERY_EXCEPTION(TransactionWithQuery.class, true);

    private final Class<?> executableInterface;
    private final boolean throwingException;

    private Kind(Class<?> executableInterface, boolean throwingException) {
      this.executableInterface = executableInterface;
      this.throwingException = throwingException;
    }

    public Class<?> getExecutableInterface() {
      return executableInterface;
    }

    public boolean isThrowingException() {
      return throwingException;
    }

  }

  public static enum Mode {
    DIRECT, QUERY, TRANSACTION
  }

  public static boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, Annotation... annotations) {
    for (Annotation annotation : annotations)
      if (annotation.annotationType() == annotationClass)
        return true;
    return false;
  }

  public static boolean isExecutionTimePresent(Annotation... annotations) {
    return isAnnotationPresent(ExecutionTime.class, annotations);
  }

  public static String now() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    return df.format(new Date());
  }

  public static String replaceLast(String string, String toReplace, String replacement) {
    int pos = string.lastIndexOf(toReplace);
    if (pos < 0)
      return string;
    return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
  }

  /**
   * Strip package name and return simple class name.
   */
  public static String simple(String name) {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  /**
   * Unmodifiable list, never null.
   */
  public static <T> List<T> unmodifiableList(List<T> list) {
    List<T> empty = Collections.emptyList();
    return (list == null || list.isEmpty()) ? empty : Collections.unmodifiableList(list);
  }

  /**
   * Return wrapper class name for primitives.
   */
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

}
