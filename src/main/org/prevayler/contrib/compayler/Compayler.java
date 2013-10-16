package org.prevayler.contrib.compayler;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.foundation.serialization.JavaSerializer;

/**
 * Prevayler Decorator Compiler.
 * 
 * <pre>
 * <code>
 * Prevalent Interface: Appendable
 * Prevalent System:    StringBuilder (implements Appendable)
 * Prevalent Decorator: AppendableDecorator (extends Decorator implements Appendable, uses Prevayler(StringBuilder))
 * </code>
 * </pre>
 * 
 * @author Christian Stein
 * @see {@linkplain http://prevayler.org}
 */
public class Compayler<PI, P extends PI> {

  /**
   * Simple command line program converting an interface into executable classes and a related decorator.
   * 
   * Usage example:
   * 
   * <pre>
   * java Compayler java.lang.Appendable src/generated
   * </pre>
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.out.println("Usage: java Compayler interface system [target path]");
      System.out.println("                      interface = prevalent system interface like 'java.lang.Appendable'");
      System.out.println("                         system = prevalent system class like 'java.lang.StringBuilder'");
      System.out.println("                    target path = optional destination folder for generated classes, defaults to '.'");
      return;
    }
    String className = args[0];
    Class<?> prevalentInterface = Class.forName(className);
    if (!prevalentInterface.isInterface()) {
      System.out.println("Interface expected, but got: " + className);
      return;
    }
    className = args[1];
    Class<?> prevalentSystemClass = Class.forName(className);
    String targetPath = ".";
    if (args.length > 2) {
      targetPath = args[2];
    }
    Util.write(new Compayler(prevalentInterface, prevalentSystemClass).generateSourcesTask().call(), targetPath);
  }

  private final Configuration<PI, P> configuration;

  /**
   * Initialize Compayler with the default configuration for the given prevalent interface.
   * 
   * @param prevalentInterface
   *          interface to compile
   */
  public Compayler(Class<PI> prevalentInterface, Class<P> prevalentSystemClass) {
    this(new Configuration<PI, P>(prevalentInterface, prevalentSystemClass));
  }

  /**
   * Initialize Compayler with the given configuration, which provides the prevalent interface.
   * 
   * @param configuration
   *          configuration to clone and use
   */
  public Compayler(Configuration<PI, P> configuration) {
    this.configuration = configuration.clone();
  }

  public Decorator<PI, P> decorate(P prevalentSystem) throws Exception {
    return decorate(prevalentSystem, "Prevalence");
  }

  public Decorator<PI, P> decorate(P prevalentSystem, File file) throws Exception {
    return decorate(prevalentSystem, file.toString());
  }

  public Decorator<PI, P> decorate(P prevalentSystem, String prevalenceDirectory) throws Exception {
    GenerateSourcesTask<PI, P> task = generateSourcesTask();
    ClassLoader loader = Util.compile(task.call(), configuration.getParentClassLoader());

    PrevaylerFactory<P> factory = new PrevaylerFactory<>();
    factory.configurePrevalentSystem(prevalentSystem);
    factory.configureJournalSerializer(new JavaSerializer(loader));
    factory.configureSnapshotSerializer(new JavaSerializer(loader));
    factory.configureTransientMode(prevalenceDirectory == null);
    if (prevalenceDirectory != null)
      factory.configurePrevalenceDirectory(prevalenceDirectory);
    return decorate(factory.create(), loader);
  }

  public Decorator<PI, P> decorate(Prevayler<P> prevayler, ClassLoader loader) throws Exception {
    String name = configuration.getPackageName() + "." + configuration.getDecoratorClassName();
    @SuppressWarnings("unchecked")
    Class<? extends Decorator<PI, P>> decoratorClass = (Class<? extends Decorator<PI, P>>) loader.loadClass(name);
    return (Decorator<PI, P>) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);

  }

  /**
   * Generate the source of the decorator class.
   * 
   * @param tags
   *          the method descriptors
   * @return source of the decorator class
   */
  public Source generateDecoratorSource(Iterable<Tag<PI>> tags) {
    List<String> lines = new LinkedList<>();

    Class<PI> prevalentInterface = configuration.getPrevalentInterface();
    String piName = Util.name(prevalentInterface);
    String pName = Util.name(configuration.getPrevalentSystemClass());
    String baseDecoName = Util.name(Decorator.class) + "<" + piName + ", " + pName + ">";
    String interfaces = piName;

    lines.add("package " + configuration.getPackageName() + ";");
    lines.add("");
    lines.add("public class " + configuration.getDecoratorClassName() + " extends " + baseDecoName + " implements " + interfaces + " {");
    lines.add("");
    lines.add("  public " + configuration.getDecoratorClassName() + "(org.prevayler.Prevayler<" + pName + "> prevayler) {");
    lines.add("    super(prevayler);");
    lines.add("  }");
    lines.add("");

    for (Tag<PI> tag : tags) {
      // custom implementation?
      List<String> customLines = tag.getCustomDecoratorMethodImplementation();
      if (customLines != null) {
        lines.addAll(customLines);
        continue;
      }
      // method head
      boolean redirect = tag.getMethod().getReturnType() == prevalentInterface;
      lines.add("  /**");
      lines.add("   * Execute new " + tag.getClassName() + tag.getParameterSignature() + " instance.");
      lines.add("   *");
      lines.add("   * Executed " + (tag.isDirect() ? "direct on the prevalent system." : "via Prevayler."));
      lines.add("   */ ");
      lines.add("  @Override");
      lines.add("  public " + tag.getMethodDeclaration() + " {");
      // direct?
      if (tag.isDirect()) {
        String invokeMethodDirect = "prevalentSystem." + tag.getMethod().getName() + tag.getParameterParentheses();
        if (tag.getMethod().getReturnType() == void.class)
          lines.add("    " + invokeMethodDirect + ";");
        else if (redirect)
          lines.add("    return redirect(" + invokeMethodDirect + ");");
        else
          lines.add("    return " + invokeMethodDirect + ";");

        lines.add("  }"); // end of method
        lines.add("");
        continue;
      }
      // instantiate executable/action and let prevayler do the work
      Class<?> executableClass = tag.getExecutableClass();
      boolean catchException = executableClass == Query.class || executableClass == TransactionWithQuery.class;
      String newAction = "new " + tag.getClassName() + tag.getParameterParentheses();
      String assignAction = tag.getExecutableDeclaration(prevalentInterface) + " action = " + newAction;
      String executeAction = "prevayler.execute(action)";
      lines.add("    " + assignAction + ";");
      if (catchException) {
        lines.add("    " + "try {");
      }
      String innerIndent = catchException ? "    " + "  " : "    ";
      if (tag.getMethod().getReturnType() == void.class) {
        lines.add(innerIndent + executeAction + ";");
      } else {
        lines.add(innerIndent + tag.getReturnTypeName() + " result = " + executeAction + ";");
        if (redirect) {
          lines.add(innerIndent + "return redirect(result);");
        } else
          lines.add(innerIndent + "return result;");
      }
      if (catchException) {
        if (tag.getMethod().getExceptionTypes().length > 0) {
          lines.add("    " + "} catch (" + tag.getMethodExceptionTypes("|").trim() + " e) {");
          lines.add("    " + "  throw e;");
        }
        if (!Arrays.asList(tag.getMethod().getExceptionTypes()).contains(Exception.class)) {
          lines.add("    " + "} catch (Exception e) {");
          lines.add("    " + "  throw new RuntimeException(e);");
        }
        lines.add("    " + "}");
      }
      lines.add("  }"); // end of method
      lines.add("");
    }
    lines.add("}"); // end of class
    lines.add("");
    return new Source(configuration.getPackageName(), configuration.getDecoratorClassName(), lines);
  }

  /**
   * Generate the source of the executable class described by the tag.
   * 
   * @param tag
   *          method descriptor
   * @return source of the executable
   */
  public Source generateExecutableSource(Tag<PI> tag) {
    List<String> lines = new LinkedList<>();
    Class<PI> prevalentInterface = configuration.getPrevalentInterface();
    lines.add("package " + configuration.getPackageName() + ";");
    lines.add("");
    lines.add("public class " + tag.getClassName() + " implements " + tag.getExecutableDeclaration(prevalentInterface) + " {");
    lines.add("");
    lines.add("  private static final long serialVersionUID = " + tag.getSerialVersionUID() + "L;");
    lines.add("");
    // fields + c'tor, if at least one parameter is present
    if (tag.getMethod().getParameterTypes().length > 0) {
      int index = 0;
      for (Class<?> param : tag.getMethod().getParameterTypes()) {
        if (index == tag.getIndexOfPrevalentDate())
          lines.add("  @SuppressWarnings(\"unused\")");
        lines.add("  private final " + Util.name(param) + " " + tag.getParameterName(index) + ";");
        index++;
      }
      lines.add("");
      lines.add("  public " + tag.getClassName() + tag.getParameterSignature() + " {");
      for (int i = 0; i < tag.getMethod().getParameterTypes().length; i++)
        lines.add("    this." + tag.getParameterName(i) + " = " + tag.getParameterName(i) + ";");
      lines.add("  }");
      lines.add("");
    }
    // implementation
    String parameters = Util.name(prevalentInterface) + " prevalentSystem, java.util.Date executionTime";
    String methodCall = tag.getMethod().getName() + tag.getParameterParenthesesWithExecutionTime();
    lines.add("  @Override");
    if (tag.getExecutableClass() == Query.class) {
      lines.add("  public " + tag.getReturnTypeWrapName() + " query(" + parameters + ") throws Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
    }
    if (tag.getExecutableClass() == Transaction.class) {
      lines.add("  public void executeOn(" + parameters + ") {");
      lines.add("    prevalentSystem." + methodCall + ";");
    }
    if (tag.getExecutableClass() == TransactionWithQuery.class) {
      lines.add("  public " + tag.getReturnTypeWrapName() + " executeAndQuery(" + parameters + ") throws Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
    }
    if (tag.getExecutableClass() == SureTransactionWithQuery.class) {
      lines.add("  public " + tag.getReturnTypeWrapName() + " executeAndQuery(" + parameters + ") {");
      lines.add("    return prevalentSystem." + methodCall + ";");
    }
    lines.add("  }"); // end of method
    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
    return new Source(configuration.getPackageName(), tag.getClassName(), lines);
  }

  /**
   * Generate a new task, that computes all sources based the configured prevalent interface. Before calling "call()" on the task instance,
   * you may customize the method decription tags.
   * 
   * @return task that computes all sources
   */
  public GenerateSourcesTask<PI, P> generateSourcesTask() {
    return new GenerateSourcesTask<>(this);
  }

  /**
   * Get the immutable configuration used by this Compayler instance.
   * 
   * @return the immutable configuration
   */
  public Configuration<PI, P> getConfiguration() {
    return configuration;
  }

}
