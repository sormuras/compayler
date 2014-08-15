package org.prevayler.contrib.compayler;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.foundation.monitor.Monitor;
import org.prevayler.foundation.monitor.SimpleMonitor;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.PrevaylerDirectory;
import org.prevayler.implementation.PrevaylerImpl;
import org.prevayler.implementation.clock.MachineClock;
import org.prevayler.implementation.journal.Journal;
import org.prevayler.implementation.journal.PersistentJournal;
import org.prevayler.implementation.publishing.CentralPublisher;
import org.prevayler.implementation.publishing.TransactionPublisher;
import org.prevayler.implementation.snapshot.GenericSnapshotManager;

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
     * Ordered stream of all modes.
     */
    public static final List<ExecutionMode> MODES = unmodifiableList(asList(TRANSACTION, QUERY, DIRECT));

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

  @SuppressWarnings("unchecked")
  public static <T> T decorate(Class<T> interfaceClass, T prevalentSystem, File folder) throws Exception {
    Compayler compayler = new Compayler(interfaceClass);
    Prevayler<T> prevayler = prevayler(prevalentSystem, prevalentSystem.getClass().getClassLoader(), folder);
    return (T) Class.forName(compayler.getDecoratorName()).getConstructor(Prevayler.class).newInstance(prevayler);
  }

  /**
   * @return default prevayler using prevalent system class loader
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem) throws Exception {
    return prevayler(prevalentSystem, prevalentSystem.getClass().getClassLoader());
  }
  
  /**
   * @return default prevayler using given class loader and {@code PrevalenceBase/} as journaling folder
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem, ClassLoader loader) throws Exception {
    return prevayler(prevalentSystem, loader, new File("PrevalenceBase"));
  }

  /**
   * @return default prevayler using given class loader and given journaling folder
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem, ClassLoader loader, File folder) throws Exception {
    PrevaylerDirectory directory = new PrevaylerDirectory(folder);
    Monitor monitor = new SimpleMonitor(System.err);
    Journal journal = new PersistentJournal(directory, 0, 0, true, "journal", monitor);
    Serializer serializer = new JavaSerializer(loader);
    Map<String, Serializer> map = Collections.singletonMap("snapshot", serializer);
    GenericSnapshotManager<P> snapshotManager = new GenericSnapshotManager<>(map, "snapshot", prevalentSystem, directory, serializer);
    Clock clock = new MachineClock();
    TransactionPublisher publisher = new CentralPublisher(clock, journal);
    boolean transactionDeepCopyMode = true;
    return new PrevaylerImpl<>(snapshotManager, publisher, serializer, transactionDeepCopyMode);
  }

  /**
   * @return default prevayler using prevalent system class loader and given journaling folder
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem, File folder) throws Exception {
    return prevayler(prevalentSystem, prevalentSystem.getClass().getClassLoader(), folder);
  }
  private String decoratorName;
  private final String interfaceName;
  private final String interfacePackage;

  private final String interfaceSimple;

  public Compayler(Class<?> interfaceClass) {
    this(interfaceClass.getPackage().getName(), interfaceClass.getName(), interfaceClass.getSimpleName());
    assert interfaceClass.isInterface() : "Interface expected, but got " + interfaceClass;
  }

  public Compayler(String interfacePackage, String interfaceName, String interfaceSimple) {
    this.interfacePackage = interfacePackage;
    this.interfaceName = interfaceName;
    this.interfaceSimple = interfaceSimple;
    this.decoratorName = buildDecoratorName();
  }

  protected String buildDecoratorName() {
    String decoratorPackage = interfacePackage.startsWith("java.") ? interfaceSimple.toLowerCase() : interfacePackage;
    return (decoratorPackage.isEmpty() ? "" : decoratorPackage + ".") + interfaceSimple.replaceAll("\\$", "") + "Decorator";
  }

  public String getDecoratorName() {
    return decoratorName;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public String getInterfacePackage() {
    return interfacePackage;
  }

  public void setDecoratorName(String decoratorName) {
    this.decoratorName = decoratorName;
  }

}
