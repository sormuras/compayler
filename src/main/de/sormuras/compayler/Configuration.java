package de.sormuras.compayler;

public class Configuration {

  /**
   * Strip package name and return simple class name.
   */
  public static String simple(String name) {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public static String stack(Class<?> c) {
    if (!c.isMemberClass())
      return c.getSimpleName();
    StringBuilder builder = new StringBuilder();
    builder.append(c.getSimpleName());
    c = c.getEnclosingClass();
    while (c != null) {
      builder.insert(0, '$');
      builder.insert(0, c.getSimpleName());
      c = c.getEnclosingClass();
    }
    return builder.toString();
  }

  /**
   * Strip and return package name.
   */
  public static String strip(String name) {
    return name.substring(0, name.lastIndexOf('.'));
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
   * "Appendable"
   */
  private String interfaceName;

  /**
   * "java.lang"
   */
  private String interfacePackage;

  public Configuration(Class<?> interfaceClass) {
    this(interfaceClass.getPackage().getName(), stack(interfaceClass));
  }

  /**
   * 
   * @param interfaceName
   *          "java.lang.Appendable"
   */
  public Configuration(String interfaceName) {
    this(strip(interfaceName), simple(interfaceName));
  }

  /**
   * 
   * @param interfacePackage
   *          "java.lang"
   * @param interfaceName
   *          "Appendable"
   */
  public Configuration(String interfacePackage, String interfaceName) {
    this.interfacePackage = interfacePackage;
    this.interfaceName = interfaceName;
    this.decoratorPackage = interfacePackage.equals("java.lang") ? interfaceName.toLowerCase() : interfacePackage;
    this.decoratorName = interfaceName + "Decorator";
  }

  /**
   * @return "com.any.api.AppendableDecorator"
   */
  public String getDecoratorClassName() {
    return decoratorPackage + '.' + decoratorName;
  }

  public String getDecoratorName() {
    return decoratorName;
  }

  public String getDecoratorPackage() {
    return decoratorPackage;
  }

  /**
   * @return "java.lang.Appendable"
   */
  public String getInterfaceClassName() {
    return interfacePackage + '.' + interfaceName;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public String getInterfacePackage() {
    return interfacePackage;
  }

}
