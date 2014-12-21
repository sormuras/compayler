package org.prevayler.contrib.p8.util.stashlet;

import java.util.HashMap;
import java.util.Map;

import org.prevayler.contrib.p8.util.Stashable;


/**
 * Collects runtime properties composing the current context.
 * 
 * <b><code>{property}</code></b> name of the property, like {@code data}<br>
 * <b><code>{type}</code></b> name of the type, like {@code java.lang.Double}<br>
 * <b><code>{source}</code></b> name of the source byte buffer, defaults to {@code source}<br>
 * <b><code>{target}</code></b> name of the target byte buffer, defaults to {@code target}<br>
 * <b><code>{spawn}</code></b> spawn method call, defaults to {@code de.codeturm.nio.Stashable.spawn}<br>
 * <b><code>{stash}</code></b> stash method call, defaults to {@code de.codeturm.nio.Stashable.stash}<br>
 * 
 * @see Context.Tag
 */
public class Context {
  
  /**
   * Stashlet scope.
   */
  public enum Scope {
    /**
     * Spawn (constructor/get) template scope.
     */
    SPAWN,

    /**
     * Stash (store/put) template scope.
     */
    STASH,
  }

  /**
   * Enumerates all well-known mustache-y tags.
   */
  public enum Tag {

    /**
     * Name of the property. Like <code>state</code> here:
     * 
     * <pre>
     * private final java.lang.Thread.State <b>state</b>;
     * </pre>
     */
    PROPERTY("?"),

    /**
     * Sourve buffer variable name, usually used in the constructor of a stashable implementation.
     */
    SOURCE("source"),

    /**
     * <b>{spawn}</b> defaults to <code>"org.prevayler.contrib.p8.util.Stashable.spawn"</code>
     */
    SPAWN(Stashable.class.getCanonicalName() + ".spawn"),

    /**
     * <b>{stash}</b> defaults to <code>"org.prevayler.contrib.p8.util.Stashable.stash"</code>
     */
    STASH(Stashable.class.getCanonicalName() + ".stash"),

    /**
     * Target buffer variable name used in the stash method.
     * 
     * @see Stashable#stash(java.nio.ByteBuffer)
     */
    TARGET("target"),

    /**
     * Fully-qualified canonical <b>{type}</b> name. Like <code>java.lang.Thread.State</code> here:
     * 
     * <pre>
     * private final <b>java.lang.Thread.State</b> state;
     * </pre>
     */
    TYPE("type");

    private final String value;

    private Tag(String value) {
      this.value = value;
    }

    public String key() {
      return "{" + name().toLowerCase() + "}";
    }

    public String value() {
      return value;
    }

  }

  private final String property;
  private final boolean nullable;
  private final Map<String, Object> map;
  private final String type;
  private final Scope scope;

  public Context(Scope scope, String property, String type, boolean nullable) {
    this.scope = scope;
    this.property = property;
    this.nullable = nullable;
    this.type = type;

    this.map = new HashMap<>();
    for (Tag tag : Tag.values())
      map.put(tag.key(), tag.value());

    map.put(Tag.PROPERTY.key(), "this." + property);
    map.put(Tag.TYPE.key(), type.replace('$', '.'));
  }

  public String getProperty() {
    return property;
  }

  public boolean isNullable() {
    return nullable;
  }

  public Scope getScope() {
    return scope;
  }

  public String getTypeName() {
    return type;
  }

  public Map<String, Object> map() {
    return map;
  }

}
