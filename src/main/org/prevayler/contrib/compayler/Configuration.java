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
public class Configuration<PI> implements Cloneable {

  private String decoratorClassName;
  private boolean direct;
  private final boolean immutable;
  private final Map<String, Boolean> methodNameUniqueMap;
  private String packageName;
  private final Class<PI> prevalentInterface;
  private Class<? extends PI> prevalentSystemClass;
  private PrevalentType prevalentType;

  /**
   * Initialize this configuration with defaults based on the prevalent interface.
   * 
   * @param prevalentInterface
   * 
   * @throws IllegalArgumentException
   *           if the prevalent interface is not an interface
   */
  public Configuration(Class<PI> prevalentInterface) {
    if (!prevalentInterface.isInterface()) {
      throw new IllegalArgumentException("Expected an interface, but got: " + prevalentInterface);
    }
    this.prevalentInterface = prevalentInterface;
    this.immutable = false;
    this.prevalentSystemClass = null;
    this.packageName = prevalentInterface.getSimpleName().toLowerCase();
    this.decoratorClassName = "Prevalent" + prevalentInterface.getSimpleName();
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
  protected Configuration(Configuration<PI> configuration) {
    this.immutable = true;
    this.prevalentInterface = configuration.prevalentInterface;
    this.decoratorClassName = configuration.decoratorClassName;
    this.direct = configuration.direct;
    this.methodNameUniqueMap = configuration.methodNameUniqueMap;
    this.packageName = configuration.packageName;
    this.prevalentSystemClass = configuration.prevalentSystemClass;
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
  public Configuration<PI> clone() {
    return new Configuration<PI>(this);
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
  public Configuration<PI> setDecoratorClassName(String decoratorClassName) {
    assertMutable();
    this.decoratorClassName = decoratorClassName;
    return this;
  }

  /**
   * Set {@code true} if the decorator should by-pass the prevayler, {@code false} otherwise.
   */
  public Configuration<PI> setDirect(boolean direct) {
    assertMutable();
    this.direct = direct;
    return this;
  }

  /**
   * Set package name, like {@code "com.abc"}.
   */
  public Configuration<PI> setPackageName(String packageName) {
    assertMutable();
    this.packageName = packageName;
    return this;
  }

  /**
   * Set prevalent system class, like {@code java.lang.StringBuilder} or {@code com.abc.DefaultAppendableImpl}.
   */
  public Configuration<PI> setPrevalentSystemClass(Class<? extends PI> prevalentSystemClass) {
    assertMutable();
    this.prevalentSystemClass = prevalentSystemClass;
    return this;
  }

  /**
   * Set default prevalent type if no prevalent method annotation is found at the prevalent interface or system class.
   */
  public Configuration<PI> setPrevalentType(PrevalentType prevalentType) {
    assertMutable();
    this.prevalentType = prevalentType;
    return this;
  }

}