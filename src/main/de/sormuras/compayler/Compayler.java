package de.sormuras.compayler;

import org.prevayler.Prevayler;

public class Compayler {

  /**
   * Helper and utility functions.
   */
  private static class Tool {

    /**
     * Attempts to locate, load, and link the interface by it's name.
     */
    private static Class<?> load(String name, ClassLoader loader) {
      try {
        return Class.forName(name, true, loader);
      } catch (ClassNotFoundException e) {
        return void.class;
      }
    }

    /**
     * Build simple class name using '$' signs for nested classes.
     */
    private static String simple(Class<?> c) {
      if (!c.isMemberClass())
        return c.getSimpleName();
      StringBuilder builder = new StringBuilder();
      builder.append(c.getSimpleName());
      while ((c = c.getEnclosingClass()) != null) {
        builder.insert(0, '$').insert(0, c.getSimpleName());
      }
      return builder.toString();
    }

    /**
     * Split package and simple class name and return one.
     */
    private static String simple(String name, boolean returnSimpleClassName) {
      int index = name.lastIndexOf('.');
      return returnSimpleClassName ? name.substring(index + 1) : name.substring(0, index);
    }
  }

  /**
   * "AppendableDecorator"
   */
  private String decoratorName;

  /**
   * "com.any.api"
   */
  private String decoratorPackage;

  /**
   * void.class or <i>interface java.lang.Appendable.class</i>
   */
  private final Class<?> interfaceClass;

  /**
   * Interface class loader.
   */
  private final ClassLoader interfaceLoader;

  /**
   * "Appendable"
   */
  private final String interfaceName;

  /**
   * "java.lang"
   */
  private final String interfacePackage;

  /**
   * @param interfaceClass
   *          the runtime interface representation
   */
  public Compayler(Class<?> interfaceClass) {
    this(interfaceClass.getPackage().getName(), Tool.simple(interfaceClass));
    assert this.interfaceClass == interfaceClass;
    assert interfaceClass.isInterface();
  }

  /**
   * 
   * @param interfaceName
   *          "java.lang.Appendable"
   */
  public Compayler(String interfaceName) {
    this(Tool.simple(interfaceName, false), Tool.simple(interfaceName, true));
  }

  /**
   * 
   * @param interfacePackage
   *          "java.lang"
   * @param interfaceName
   *          "Appendable"
   */
  public Compayler(String interfacePackage, String interfaceName) {
    this(interfacePackage, interfaceName, Thread.currentThread().getContextClassLoader());
  }

  /**
   * @param interfacePackage
   *          "java.lang"
   * @param interfaceName
   *          "Appendable"
   * @param interfaceLoader
   *          the class loader that is requested loading the interface class and used as parent for compilation
   */
  public Compayler(String interfacePackage, String interfaceName, ClassLoader interfaceLoader) {
    this.interfacePackage = interfacePackage;
    this.interfaceName = interfaceName;
    this.interfaceLoader = interfaceLoader;
    this.interfaceClass = Tool.load(getInterfaceClassName(), getInterfaceLoader());
    setDecoratorPackage(interfacePackage.startsWith("java.") ? interfaceName.toLowerCase() : interfacePackage);
    setDecoratorName(interfaceName.replaceAll("\\$", "") + "Decorator");
  }

  public void compile() {
    // TODO generate decorator source file
    // TODO comile decorator source
    // TODO set decorator loader
  }

  public <P> P decorate(P prevalentSystem) throws Exception {
    return decorate(prevalentSystem, "PrevalenceBase");
  }

  public <P> P decorate(P prevalentSystem, String directory) throws Exception {
    ClassLoader loader = getInterfaceLoader(); // TODO compile() -> getDecoratorLoader()
    Prevayler<P> prevayler = new PrevaylerSupport.VolatilePrevayler<>(prevalentSystem, loader);
    if (directory != null)
      prevayler = PrevaylerSupport.createPrevayler(prevalentSystem, loader, directory);
    @SuppressWarnings("unchecked")
    Class<? extends P> decoratorClass = (Class<? extends P>) loader.loadClass(getDecoratorClassName());
    return decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  public String getDecoratorClassName() {
    return decoratorPackage + '.' + decoratorName;
  }

  public String getDecoratorName() {
    return decoratorName;
  }

  public String getDecoratorPackage() {
    return decoratorPackage;
  }

  public Class<?> getInterfaceClass() {
    return interfaceClass;
  }

  public String getInterfaceClassName() {
    return interfacePackage + '.' + interfaceName;
  }

  public ClassLoader getInterfaceLoader() {
    return interfaceLoader;
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

  public void setDecoratorPackage(String decoratorPackage) {
    this.decoratorPackage = decoratorPackage;
  }

}
