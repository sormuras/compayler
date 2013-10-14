package org.prevayler.contrib.compayler;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

/**
 * Prevayler Decorator Compiler.
 * 
 * <pre>
 * Prevalent Interface: Appendable
 * Prevalent System:    StringBuilder (implements Appendable)
 * Prevalent Decorator: PrevalentAppendable (implements Appendable & PrevaylerDecorator)
 * </pre>
 * 
 * @author Christian Stein
 * @see {@linkplain http://prevayler.org}
 */
public class Compayler<PI> {

  /**
   * Simple command line program converting an interface into executable classes and a related decorator.
   * 
   * Usage example:
   * 
   * <pre>
   * java Compayler java.lang.Appendable src/generated
   * </pre>
   */
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java Compayler interface [target path]");
      System.out.println("                      interface = prevalent system interface like 'java.lang.Appendable'");
      System.out.println("                    target path = optional destination folder for generated classes, defaults to '.'");
      return;
    }
    String className = args[0];
    Class<?> prevalentInterface = Class.forName(className);
    if (!prevalentInterface.isInterface()) {
      System.out.println("Interface expected, but got: " + className);
      return;
    }
    String targetPath = ".";
    if (args.length > 1) {
      targetPath = args[1];
    }
    Util.write(new Compayler<>(prevalentInterface).generateSourcesTask().call(), targetPath);
  }

  private final Configuration<PI> configuration;

  /**
   * Initialize Compayler with the default configuration for the given prevalent interface.
   * 
   * @param prevalentInterface
   *          interface to compile
   */
  public Compayler(Class<PI> prevalentInterface) {
    this(new Configuration<PI>(prevalentInterface));
  }

  /**
   * Initialize Compayler with the given configuration, which provides the prevalent interface.
   * 
   * @param configuration
   *          configuration to clone and use
   */
  public Compayler(Configuration<PI> configuration) {
    this.configuration = configuration.clone();
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
    String interfaces = piName + ", " + PrevaylerDecorator.class.getCanonicalName() + "<" + piName + ">";
    String piField = prevalentInterface.getSimpleName().toLowerCase().charAt(0) + prevalentInterface.getSimpleName().substring(1);

    lines.add("package " + configuration.getPackageName() + ";");
    lines.add("");
    lines.add("public class " + configuration.getDecoratorClassName() + " implements " + interfaces + " {");
    lines.add("");
    lines.add("  private final org.prevayler.Prevayler<" + piName + "> prevayler;");
    lines.add("  private final " + piName + " " + piField + ";");
    lines.add("");
    lines.add("  public " + configuration.getDecoratorClassName() + "(org.prevayler.Prevayler<" + piName + "> prevayler) {");
    lines.add("    this.prevayler = prevayler;");
    lines.add("    this." + piField + " = prevayler.prevalentSystem();");
    lines.add("  }");
    lines.add("");
    lines.add("  @Override");
    lines.add("  public org.prevayler.Prevayler<" + piName + "> prevayler() {");
    lines.add("    return prevayler;");
    lines.add("  }");
    lines.add("");
    lines.add("  protected " + piName + " redirect(" + piName + " result) {");
    lines.add("    return result == " + piField + " ? this : result;");
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
        String invokeMethodDirect = piField + "." + tag.getMethod().getName() + tag.getParameterParentheses();
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
        lines.add("    " + "} catch (" + tag.getMethodExceptionTypes("|").trim() + " e) {");
        lines.add("    " + "  throw e;");
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
  public GenerateSourcesTask<PI> generateSourcesTask() {
    return new GenerateSourcesTask<>(this);
  }

  /**
   * Get the immutable configuration used by this Compayler instance.
   * 
   * @return the immutable configuration
   */
  public Configuration<PI> getConfiguration() {
    return configuration;
  }

  /**
   * Load decorator class, create new instance and pass the given prevayler to it.
   * 
   * @param loader
   *          class loader to load the decorator class
   * @param prevayler
   *          prevayler instance to decorate
   * @return decorator instance decorating the prevayler
   * @throws Exception
   *           if any
   * 
   * @see #toDecorator(PrevaylerCreator)
   */
  public PrevaylerDecorator<PI> toDecorator(ClassLoader loader, Prevayler<PI> prevayler) throws Exception {
    String name = configuration.getPackageName() + "." + configuration.getDecoratorClassName();
    @SuppressWarnings("unchecked")
    Class<? extends PrevaylerDecorator<PI>> decoratorClass = (Class<? extends PrevaylerDecorator<PI>>) loader.loadClass(name);
    return (PrevaylerDecorator<PI>) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  /**
   * Generate & compile all sources, create prevayler instance using the creator, load decorator class, create new instance and pass the
   * prevayler to it.
   * 
   * @param parent
   *          class loader used as the parent of the newly created one that hosts the compiled classes
   * @param creator
   *          prevayler creator
   * @return decorator instance decorating the created prevayler
   * @throws Exception
   *           if any
   */
  public PrevaylerDecorator<PI> toDecorator(ClassLoader parent, PrevaylerCreator<PI> creator) throws Exception {
    GenerateSourcesTask<PI> task = generateSourcesTask();
    ClassLoader loader = Util.compile(task.call(), parent);
    Prevayler<PI> prevayler = creator.createPrevayler(loader);
    return toDecorator(loader, prevayler);
  }

  /**
   * Same as {@code toDecorator(getClass().getClassLoader(), creator)}.
   */
  public PrevaylerDecorator<PI> toDecorator(PrevaylerCreator<PI> creator) throws Exception {
    return toDecorator(getClass().getClassLoader(), creator);
  }
}
