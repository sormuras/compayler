package de.sormuras.compayler;

public class Configuration {

  public static String simple(Class<?> c) {
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
  public static String simple(String name, boolean returnSimpleClassName) {
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
   * null or <i>interface java.lang.Appendable.class</i>
   */
  private Class<?> interfaceClass;

  /**
   * "Appendable"
   */
  private final String interfaceName;

  /**
   * "java.lang"
   */
  private final String interfacePackage;

  public Configuration(Class<?> interfaceClass) {
    this(interfaceClass.getPackage().getName(), simple(interfaceClass));
    this.interfaceClass = interfaceClass;
  }

  /**
   * 
   * @param interfaceName
   *          "java.lang.Appendable"
   */
  public Configuration(String interfaceName) {
    this(simple(interfaceName, false), simple(interfaceName, true));
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
    setDecoratorPackage(interfacePackage.startsWith("java.") ? interfaceName.toLowerCase() : interfacePackage);
    setDecoratorName(interfaceName.replaceAll("\\$", "") + "Decorator");
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

  public Class<?> getInterfaceClass() throws ClassNotFoundException {
    if (interfaceClass == null) {
      interfaceClass = Class.forName(getInterfaceClassName());
    }
    return interfaceClass;
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

  public void setDecoratorName(String decoratorName) {
    this.decoratorName = decoratorName;
  }

  public void setDecoratorPackage(String decoratorPackage) {
    this.decoratorPackage = decoratorPackage;
  }

}
