package org.prevayler.contrib.p8.util.stashlet;

import org.prevayler.contrib.p8.util.stashlet.Context.Scope;

/**
 * Stashable template encapsulates mustache-looking Java source code snippets.
 * <p>
 * Reading/spawning an Integer from a byte buffer (nullable):<br>
 * <code>{property} = {spawn}({source}, (s) -&gt; Integer.valueOf(s.getInt()));</code>
 * <p>
 * Reading/spawning an enum value (not nullable):<br>
 * <code>{property} = {type}.values()[{source}.getInt()];</code>
 * 
 * @see Context
 */
public interface Stashlet {

  /**
   * Get the scope-related template string.
   * 
   * @param scope
   *          the scope
   * @param nullable
   *          if the value/property is nullable
   * @return the template string
   * @throws UnsupportedOperationException
   *           if the described type does not fit the scope/nullable configuration
   */
  default String get(Scope scope, boolean nullable) {
    switch (scope) {
    case SPAWN:
      return nullable ? getSpawnNullable() : getSpawnStraight();
    case STASH:
      return nullable ? getStashNullable() : getStashStraight();
    }
    throw new UnsupportedOperationException("Out of scope?! " + scope);
  }

  /**
   * @return the constructing and <b>null-safe</b> code template, re-creating an object from reading the source buffer bytes
   */
  String getSpawnNullable();

  /**
   * @return the constructing code template, re-creating an object (which is never {@code null}) from reading the source buffer bytes
   */
  default String getSpawnStraight() {
    return getSpawnNullable();
  }

  /**
   * @return the <b>null-aware</b> store code template, writing an object to the target byte buffer
   */
  String getStashNullable();

  /**
   * @return the store code template, writing an object (which is never {@code null}) to the target byte buffer
   */
  default String getStashStraight() {
    return getStashNullable();
  }

  /**
   * Get the described type name.
   * 
   * @return the name of the described type, like {@link Class#getTypeName()}
   */
  String getTypeName();

  /**
   * Get the nullable behaviour of this implementation.
   * 
   * All eight primitive data types (<code>boolean, byte, char, double, float, int, long, short</code>) are not nullable.
   * 
   * @return {@code true} if nullable, else {@code false}
   */
  default boolean isTypeNullable() {
    return true;
  }

}
