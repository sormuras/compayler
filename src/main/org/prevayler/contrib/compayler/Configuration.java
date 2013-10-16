package org.prevayler.contrib.compayler;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Compayler configuration bean.
 * 
 * @author Christian Stein
 */
public class Configuration<PI, P extends PI> implements Cloneable {

  private String decoratorClassName;
  private boolean direct;
  private final boolean immutable;
  private final Map<String, Boolean> methodNameUniqueMap;
  private String packageName;
  private ClassLoader parentClassLoader;
  private final Class<PI> prevalentInterface;
  private final Class<P> prevalentSystemClass;
  private PrevalentType prevalentType;

  /**
   * Initialize this configuration with defaults based on the prevalent interface.
   * 
   * @param prevalentInterface
   * 
   * @throws IllegalArgumentException
   *           if the prevalent interface is not an interface
   */
  public Configuration(Class<PI> prevalentInterface, Class<P> prevalentSystemClass) {
    if (!prevalentInterface.isInterface()) {
      throw new IllegalArgumentException("Expected an interface, but got: " + prevalentInterface);
    }
    if (prevalentSystemClass.isInterface()) {
      throw new IllegalArgumentException("Expected type class, but got: " + prevalentSystemClass);
    }
    this.prevalentInterface = prevalentInterface;
    this.prevalentSystemClass = prevalentSystemClass;
    this.immutable = false;
    this.parentClassLoader = getClass().getClassLoader();
    this.packageName = prevalentInterface.getSimpleName().toLowerCase();
    this.decoratorClassName = prevalentInterface.getSimpleName() + "Decorator";
    this.methodNameUniqueMap = Collections.unmodifiableMap(buildMethodNameUniqueMap());
    try {
      this.direct = (boolean) PrevalentMethod.class.getMethod("direct").getDefaultValue();
      this.prevalentType = (PrevalentType) PrevalentMethod.class.getMethod("value").getDefaultValue();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new Error(e);
    }
  }

  /**
   * Copy constructor initializing an immutable configuration.
   * 
   * @param configuration
   *          the configuration to copy
   */
  protected Configuration(Configuration<PI, P> configuration) {
    this.immutable = true;
    this.prevalentInterface = configuration.prevalentInterface;
    this.prevalentSystemClass = configuration.prevalentSystemClass;
    this.decoratorClassName = configuration.decoratorClassName;
    this.direct = configuration.direct;
    this.methodNameUniqueMap = configuration.methodNameUniqueMap;
    this.packageName = configuration.packageName;
    this.parentClassLoader = configuration.parentClassLoader;
    this.prevalentType = configuration.prevalentType;
  }

  /**
   * Called by setters.
   * 
   * @throws IllegalStateException
   *           if this configuration is immutable
   */
  protected void assertMutable() {
    if (immutable)
      throw new IllegalStateException("This configuration is not mutable! " + this);
  }

  /**
   * 4-times "Boolean.TRUE" for {@code java.lang.CharSequence} and 3-times "Boolean.FALSE" for {@code java.lang.Appendable}.
   * 
   * @return map of method name to uniqueness pairs
   */
  protected Map<String, Boolean> buildMethodNameUniqueMap() {
    Map<String, Boolean> uniques = new HashMap<>();
    for (Method method : prevalentInterface.getMethods()) {
      Boolean old = uniques.put(method.getName(), Boolean.TRUE);
      if (old != null)
        uniques.put(method.getName(), Boolean.FALSE);
    }
    return uniques;
  }

  @Override
  public Configuration<PI, P> clone() {
    return new Configuration<PI, P>(this);
  }

  /**
   * Create a method description tag.
   * 
   * @param method
   *          method to analyze
   * @return method description tag
   */
  public Tag<PI> createTag(Method method) {
    boolean direct = isDirect();
    PrevalentType type = getPrevalentType();

    if (method.isAnnotationPresent(PrevalentMethod.class)) {
      direct = method.getAnnotation(PrevalentMethod.class).direct();
      type = method.getAnnotation(PrevalentMethod.class).value();
    }
    Class<? extends PI> prevalentSystemClass = getPrevalentSystemClass();
    if (prevalentSystemClass != null) {
      try {
        Method implementation = prevalentSystemClass.getMethod(method.getName(), method.getParameterTypes());
        if (implementation.isAnnotationPresent(PrevalentMethod.class)) {
          direct = implementation.getAnnotation(PrevalentMethod.class).direct();
          type = implementation.getAnnotation(PrevalentMethod.class).value();
        }
      } catch (NoSuchMethodException | SecurityException e) {
        // ignore, use configured defaults
      }
    }

    Tag<PI> tag = new Tag<PI>(method, methodNameUniqueMap.get(method.getName()));
    tag.setDirect(direct);
    tag.setType(type);
    tag.setSerialVersionUID(1L);
    return tag;
  }

  /**
   * @return simple name of the decorator class, like {@code "PrevalentAppendable"}
   */
  public String getDecoratorClassName() {
    return decoratorClassName;
  }

  /**
   * @return java package name, like {@code "org.prevayler"}
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * @return parent class loader, like {@code getClass().getClassLoader()}.
   */
  public ClassLoader getParentClassLoader() {
    return parentClassLoader;
  }

  /**
   * @return prevalent interface type, like {@link java.lang.Appendable}
   */
  public Class<PI> getPrevalentInterface() {
    return prevalentInterface;
  }

  /**
   * @return prevalent system class, like {@code java.lang.StringBuilder} or {@code com.abc.DefaultAppendableImpl}
   */
  public Class<? extends PI> getPrevalentSystemClass() {
    return prevalentSystemClass;
  }

  /**
   * @return default prevalent type if no prevalent method annotation is found at the prevalent interface or system class
   */
  public PrevalentType getPrevalentType() {
    return prevalentType;
  }

  /**
   * @return {@code true} if the decorator should by-pass the prevayler, {@code false} otherwise
   */
  public boolean isDirect() {
    return direct;
  }

  /**
   * @return {@code true} if this configuration is unmodifiable, else {@code false}
   */
  public boolean isImmutable() {
    return immutable;
  }

  /**
   * Set simple name of the decorator class, like {@code "PrevalentAppendable"}.
   */
  public Configuration<PI, P> setDecoratorClassName(String decoratorClassName) {
    assertMutable();
    this.decoratorClassName = decoratorClassName;
    return this;
  }

  /**
   * Set {@code true} if the decorator should by-pass the prevayler, {@code false} otherwise.
   */
  public Configuration<PI, P> setDirect(boolean direct) {
    assertMutable();
    this.direct = direct;
    return this;
  }

  /**
   * Set package name, like {@code "com.abc"}.
   */
  public Configuration<PI, P> setPackageName(String packageName) {
    assertMutable();
    this.packageName = packageName;
    return this;
  }

  /**
   * Set parent class loader, like {@code getClass().getClassLoader()}.
   */
  public Configuration<PI, P> setParentClassLoader(ClassLoader parentClassLoader) {
    assertMutable();
    this.parentClassLoader = parentClassLoader;
    return this;
  }

  /**
   * Set default prevalent type if no prevalent method annotation is found at the prevalent interface or system class.
   */
  public Configuration<PI, P> setPrevalentType(PrevalentType prevalentType) {
    assertMutable();
    this.prevalentType = prevalentType;
    return this;
  }

}
