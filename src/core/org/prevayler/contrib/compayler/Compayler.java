package org.prevayler.contrib.compayler;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import org.prevayler.Prevayler;
import org.prevayler.contrib.compayler.javac.Source;
import org.prevayler.contrib.compayler.prevayler.PrevaylerFactory;

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

  private final String interfaceName;

  public Compayler(Class<?> interfaceClass) {
    this(interfaceClass.getName());
    assert interfaceClass.isInterface() : "Interface expected, but got " + interfaceClass;
  }

  public Compayler(String interfaceName) {
    this.interfaceName = interfaceName;
  }

  public Source compile() throws Exception {
    ClassLoader loader = getClass().getClassLoader();
    URL resource = loader.getResource(interfaceName.replace('.', '/') + ".java");

    File file = new File(resource.toURI());
    Processor processor = new Processor();
    Source source = new Source(interfaceName, Files.readAllLines(file.toPath()));
    // source.getCompilerOptions().add("-proc:only");
    source.getCompilerProcessors().add(processor);

    return source;
  }

  public <P> P decorate(PrevaylerFactory<P> prevaylerFactory) throws Exception {
    return decorate(prevaylerFactory, compile().compile());
  }

  public <P> P decorate(PrevaylerFactory<P> prevaylerFactory, ClassLoader loader) throws Exception {
    Prevayler<P> prevayler = prevaylerFactory.createPrevayler(loader);
    @SuppressWarnings("unchecked")
    Class<? extends P> decoratorClass = (Class<? extends P>) loader.loadClass(getDecoratorName());
    return decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  public String getDecoratorName() {
    String simple = Generator.simple(interfaceName);
    String interfacePackage = Generator.packaged(interfaceName);
    String decoratorPackage = interfacePackage.startsWith("java.") ? simple.toLowerCase() : interfacePackage;
    return (decoratorPackage.isEmpty() ? "" : decoratorPackage + ".") + simple.replaceAll("\\$|\\.", "") + "Decorator";
  }

  public String getInterfaceName() {
    return interfaceName;
  }

}
