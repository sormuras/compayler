package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.DIRECT;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.QUERY;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.TRANSACTION;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.MirroredTypeException;

/**
 * Prevayler decorator compiler main class and configuration assets.
 * 
 * @author Christian Stein
 */
public class Compayler {

  /**
   * Interface-level annotation.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @Decorate
  public @interface Decorate {

    /**
     * @return the regular expression matching method names for direct execution mode.
     * @see ExecutionMode#DIRECT
     */
    String directRegex() default ".*[d|D]irect.*|.*[t|T]ransient.*";

    /**
     * @return the regular expression matching method names for sensitive query execution mode.
     * @see ExecutionMode#QUERY
     */
    String queryRegex() default "^get.*|^is.*|^find.*|^fetch.*|^retrieve.*";

    /**
     * @return the class the generated decorator will extend.
     */
    Class<?> superClass() default Object.class;

    /**
     * @return the regular expression matching method names for transaction execution mode.
     * @see ExecutionMode#TRANSACTION
     */
    String transactionRegex() default ".*";

    /**
     * @return the canonical name of the decorator class, or an empty string indicating automatic generation of the name.
     */
    String value() default "";
  }

  /**
   * Method-level annotation overriding interface-level settings.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Execute {

    long serialVersionUID() default 0L;

    ExecutionMode value() default ExecutionMode.TRANSACTION;

  }

  /**
   * Execution mode.
   */
  public enum ExecutionMode {

    /**
     * By-pass decorator/prevayler and work directly on the underlying prevalent system.
     * <p>
     * Copied from prevayler javadoc:<br>
     * Robust Queries (queries that do not affect other operations and that are not affected by them) can be <b>executed directly</b> as
     * plain old method calls on the prevalentSystem() without the need of being implemented as Query objects. Examples of Robust Queries
     * are queries that read the value of a single field or historical queries such as: "What was this account's balance at mid-night?"
     */
    DIRECT,

    /**
     * Query-only, which is not persisted by prevayler.
     * <p>
     * Copied from prevayler javadoc:<br>
     * A sensitive query that would be affected by the concurrent execution of a Transaction or other sensitive queries. This method
     * <b>synchronizes</b> on the prevalentSystem() to execute the sensitive query. It is therefore guaranteed that no other Transaction or
     * sensitive query is executed at the same time.
     */
    QUERY,

    /**
     * Transactions are journaled for system recovery.
     * <p>
     * Copied from prevayler javadoc:<br>
     * This method <b>synchronizes</b> on the prevalentSystem() to execute the Transaction. It is therefore guaranteed that only one
     * Transaction is executed at a time. This means the prevalentSystem() does not have to worry about concurrency issues among
     * Transactions. Implementations of this interface can log the given Transaction for crash or shutdown recovery, for example, or execute
     * it remotely on replicas of the prevalentSystem() for fault-tolerance and load-balancing purposes.
     */
    TRANSACTION;

    /**
     * Ordered list of all modes.
     */
    public static ExecutionMode forName(String name, Map<ExecutionMode, Matcher> matchers) {
      for (ExecutionMode mode : values()) {
        if (matchers.get(mode).reset(name).matches())
          return mode;
      }
      throw new IllegalArgumentException(String.format("No match for name \"%s\" in %s", name, matchers));
    }

  }

  /**
   * Execution time annotation.
   * <p>
   * {@code long getTime(@ExecutionTime Date time)}
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface ExecutionTime {
    // empty
  }

  private final Decorate decorate;
  private final Map<ExecutionMode, Matcher> executionModeMatchers;
  private final String interfaceName;
  private final String interfacePackage;
  private final String interfaceSimple;

  public Compayler(Class<?> interfaceClass) {
    this(interfaceClass.getAnnotation(Decorate.class), interfaceClass.getPackage().getName(), interfaceClass.getName(), interfaceClass
        .getSimpleName());
    assert interfaceClass.isInterface() : "Interface expected, but got " + interfaceClass;
    assert decorate != null : "Not even a default @Decorate annotation found?!";
  }

  public Compayler(Decorate decorate, String interfacePackage, String interfaceName, String interfaceSimple) {
    this.decorate = decorate == null ? Decorate.class.getAnnotation(Decorate.class) : decorate;
    this.interfacePackage = interfacePackage;
    this.interfaceName = interfaceName;
    this.interfaceSimple = interfaceSimple;
    this.executionModeMatchers = buildExecutionModeMatchers();
  }

  protected String buildDecoratorName() {
    String decoratorPackage = interfacePackage.startsWith("java.") ? interfaceSimple.toLowerCase() : interfacePackage;
    return (decoratorPackage.isEmpty() ? "" : decoratorPackage + ".") + interfaceSimple + "Decorator";
  }

  protected Map<ExecutionMode, Matcher> buildExecutionModeMatchers() {
    Map<ExecutionMode, Matcher> map = new EnumMap<>(ExecutionMode.class);
    map.put(TRANSACTION, Pattern.compile(decorate.transactionRegex()).matcher(""));
    map.put(QUERY, Pattern.compile(decorate.queryRegex()).matcher(""));
    map.put(DIRECT, Pattern.compile(decorate.directRegex()).matcher(""));
    return map;
  }

  public Decorate getDecorateAnnotation() {
    return decorate;
  }

  public String getDecoratorName() {
    return decorate.value().isEmpty() ? buildDecoratorName() : decorate.value();
  }

  public Map<ExecutionMode, Matcher> getExecutionModeMatchers() {
    return executionModeMatchers;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public String getInterfacePackage() {
    return interfacePackage;
  }

  public Class<?> getSuperClass() {
    try {
      return decorate.superClass();
    } catch (MirroredTypeException mte) {
      try {
        return Class.forName(mte.getTypeMirror().toString());
      } catch (ClassNotFoundException e) {
        return Object.class;
      }
    }
  }

}
