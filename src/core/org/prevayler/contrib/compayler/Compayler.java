package org.prevayler.contrib.compayler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
  public static @interface Decorate {

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
     * @return the regular expression matching method names for transaction execution mode.
     * @see ExecutionMode#TRANSACTION
     */
    String transactionRegex() default ".*";

    /**
     * @return the class the generated decorator will extend.
     */
    Class<?> superClass() default Object.class;

    /**
     * @return the binary name of the decorator class, or an empty string indicating automatic generation of the name.
     */
    String value() default "";
  }

  /**
   * Method-level annotation overriding interface-level settings.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Executable {
    ExecutionMode value() default ExecutionMode.TRANSACTION;
  }

  /**
   * Execution mode.
   */
  public static enum ExecutionMode {

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
    TRANSACTION
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

}
