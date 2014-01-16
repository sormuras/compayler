package de.sormuras.compayler;

import org.prevayler.Prevayler;

public class Compayler {

  public static <P> P decorate(Class<P> interfaceClass, P prevalentSystem, String directory) throws Exception {
    Compayler compayler = new Compayler(interfaceClass);
    ClassLoader loader = Compayler.class.getClassLoader();
    Prevayler<P> prevayler = PrevaylerSupport.createPrevayler(prevalentSystem, loader, directory);
    @SuppressWarnings("unchecked")
    Class<? extends P> decoratorClass = (Class<? extends P>) loader.loadClass(compayler.getDecoratorClassName());
    return decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  /**
   * Given the fully qualified name for an interface this method attempts to locate, load, and link the interface.
   */
  private static Class<?> load(String name, ClassLoader loader, Class<?> fallBack) {
    try {
      return Class.forName(name, true, loader);
    } catch (ClassNotFoundException e) {
      return fallBack;
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
    this(interfaceClass.getPackage().getName(), simple(interfaceClass));
    assert this.interfaceClass == interfaceClass;
    assert interfaceClass.isInterface();
  }

  /**
   * 
   * @param interfaceName
   *          "java.lang.Appendable"
   */
  public Compayler(String interfaceName) {
    this(simple(interfaceName, false), simple(interfaceName, true));
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
   * @param loader
   *          the class loader that is requested loading the interface class
   */
  public Compayler(String interfacePackage, String interfaceName, ClassLoader loader) {
    this.interfacePackage = interfacePackage;
    this.interfaceName = interfaceName;
    this.interfaceClass = load(getInterfaceClassName(), loader, void.class);
    setDecoratorPackage(interfacePackage.startsWith("java.") ? interfaceName.toLowerCase() : interfacePackage);
    setDecoratorName(interfaceName.replaceAll("\\$", "") + "Decorator");
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
