package org.prevayler.contrib.p8.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * ByteBuffer-backed serialization framework master interface and utilities.
 * 
 * Example:
 * 
 * <pre>
 * <code>
 * public class Data implements Stashable {
 * 
 *   private final int i;
 *   private final java.lang.Double d;
 *   private final java.util.concurrent.TimeUnit unit;
 *   private final java.util.BitSet bits;
 *   private final Data alpha;
 * 
 *   protected Data(java.nio.ByteBuffer source) {
 *     this.i = source.getInt();
 *     this.d = Stashable.spawn(source, (s) -> Double.valueOf(s.getDouble()));
 *     this.unit = Stashable.spawn(source, (s) -> java.util.concurrent.TimeUnit.values()[source.getInt()]);
 *     this.alpha = Stashable.spawn(source, Data::new);
 *     this.bits = Stashable.spawn(source, java.util.BitSet.class);
 *   }
 * 
 *   public java.nio.ByteBuffer stash(java.nio.ByteBuffer target) {
 *     target.putInt(i);
 *     Stashable.stash(target, target::putDouble, d);
 *     Stashable.stash(target, unit, (t) -> t.putInt(unit.ordinal()));
 *     Stashable.stash(target, alpha);
 *     Stashable.stash(target, bits);
 *     return target;
 *   }
 * 
 * }
 * <code>
 * </pre>
 * 
 * @author Christian Stein
 */
public interface Stashable {

  /**
   * Convenient interface for {@code Function<ByteBuffer, R extends Stashable>}.
   * 
   * @param <S>
   *          return type
   */
  @FunctionalInterface
  interface Constructor<S extends Stashable> extends Function<ByteBuffer, S> {
    // empty on purpose
  }

  /**
   * ObjectInputStream factory.
   */
  @FunctionalInterface
  public interface SpawnStreamFactory {

    /**
     * Create an ObjectInputStream that reads from the specified InputStream.
     * 
     * @param input
     *          input stream to read from
     * @return newly created object input stream
     * @throws IOException
     */
    ObjectInputStream create(InputStream input) throws IOException;

  }

  /**
   * Validate a class for proper {@link Stashable} implementation.
   * 
   * @param candidate
   *          class to validate
   * @throws IllegalArgumentException
   *           if candidate doesn't meet all requirements
   */
  static boolean check(Class<?> candidate) throws IllegalArgumentException {
    if (candidate == null)
      throw new NullPointerException("Can't check(null)!");
    if (!Stashable.class.isAssignableFrom(candidate))
      throw new IllegalArgumentException(candidate + " doesn't implement " + Stashable.class + "!");
    try {
      candidate.getDeclaredConstructor(ByteBuffer.class);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(candidate + " doesn't provide constructor with " + ByteBuffer.class + " as single parameter!", e);
    }
    return true;
  }

  /**
   * Create stashable constructor using default method handles lookup object.
   * 
   * @param type
   *          class to be constructed
   * @return constructor function
   * @see MethodHandles#lookup()
   */
  static <S extends Stashable> Constructor<S> constructor(Class<S> type) {
    return constructor(type, MethodHandles.lookup());
  }

  /**
   * Create stashable constructor using specific method handles lookup object.
   * 
   * @param type
   *          class to be constructed
   * @param lookup
   *          lookup object to use
   * @return constructor function
   */
  static <S extends Stashable> Constructor<S> constructor(Class<S> type, MethodHandles.Lookup lookup) {
    assert Stashable.check(type);
    boolean noMethodsDeclared = Constructor.class.getDeclaredMethods().length == 0;
    MethodType constructorType = MethodType.methodType(void.class, ByteBuffer.class);
    MethodType returnType = MethodType.methodType(Constructor.class);
    MethodType applyType = MethodType.methodType(type, ByteBuffer.class);
    MethodType samType = noMethodsDeclared ? applyType.generic() : applyType.changeReturnType(Stashable.class);
    try {
      MethodHandle handle = lookup.findConstructor(type, constructorType);
      CallSite callsite = LambdaMetafactory.metafactory(lookup, "apply", returnType, samType, handle, applyType);
      return (Constructor<S>) callsite.getTarget().invokeExact();
    } catch (Throwable t) {
      throw new Error(t);
    }
  }

  /**
   * Get next stored object from the specified byte buffer using default deserialization and default class loader.
   * 
   * @param source
   *          byte buffer to read from
   * @param type
   *          the exact type of the expected stored object
   * @return the object read, can be {@code null}
   */
  static <T> T spawn(ByteBuffer source, Class<T> type) {
    return spawn(source, type, ObjectInputStream::new);
  }

  /**
   * Get next stored object from the specified byte buffer using default deserialization.
   * 
   * @param source
   *          byte buffer to read from
   * @param type
   *          the exact type of the expected stored object
   * @param streamFactory
   *          the factory creating an ObjectInputStream over the byte buffer-backed input stream
   * @return the object read, can be {@code null}
   */
  static <T> T spawn(ByteBuffer source, Class<T> type, SpawnStreamFactory streamFactory) {
    byte mode = source.get();
    switch (mode) {
    case 0:
      return null;
    case 1:
      throw new IllegalStateException("Mode 1 not allowed here!\n" + Nio.toString(source));
    case 2:
      try (ObjectInputStream stream = streamFactory.create(new ByteBufferInputStream(source))) {
        @SuppressWarnings("unchecked")
        T object = (T) stream.readObject();
        return object;
      } catch (Exception e) {
        throw new RuntimeException("Couldn't read object from byte buffer stream!", e);
      }
    default:
      throw new RuntimeException("Unknown spawn mode #" + mode + "?!");
    }
  }

  /**
   * Get next stored stashable object from the specified byte buffer.
   * 
   * @param source
   *          byte buffer to read from
   * @param function
   *          reads from byte buffer, initializes the object to be returned
   * @return the object read, can be {@code null}
   */
  static <R> R spawn(ByteBuffer source, Function<ByteBuffer, R> function) {
    byte mode = source.get();
    switch (mode) {
    case 0:
      return null;
    case 1:
      return function.apply(source);
    case 2:
      throw new IllegalStateException("Mode 2 not allowed here!\n" + Nio.toString(source));
    default:
      throw new RuntimeException("Unknown spawn mode #" + mode + "?!");
    }
  }

  /**
   * Example: {@code Stashable.stash(target, target::putDouble, value);}
   * 
   * @param target
   * @param function
   * @param object
   * @return
   */
  static <T> ByteBuffer stash(ByteBuffer target, Function<T, ByteBuffer> function, T object) {
    return stash(target, object, (buffer) -> function.apply(object));
  }

  /**
   * Store a serializable/externalizable object into the specified target buffer.
   * 
   * @param target
   *          buffer to write to
   * @param object
   *          the object to write, {@code null} is permitted
   * @return target buffer
   */
  static ByteBuffer stash(ByteBuffer target, Object object) {
    if (object != null) {
      assert object instanceof Serializable : object.getClass() + " is not an instance of " + Serializable.class;
      assert !(object instanceof Stashable) : "This should not happen, there's a Stashable.stash(ByteBuffer, Stashable) method here!?";
    }
    return stash(target, object, null);
  }

  /**
   * Store an object into target buffer.
   * 
   * 
   * @param target
   *          buffer to write to
   * @param object
   *          the object to write, {@code null} is permitted
   * @param operator
   *          storing operator working on and returning the target byte buffer
   * @return target buffer
   */
  static ByteBuffer stash(ByteBuffer target, Object object, UnaryOperator<ByteBuffer> operator) {
    if (object == null)
      return target.put((byte) 0);

    if (operator != null)
      return operator.apply(target.put((byte) 1));

    target.put((byte) 2);
    try (ObjectOutputStream stream = new ObjectOutputStream(new ByteBufferOutputStream(target))) {
      stream.writeObject(object);
    } catch (Exception e) {
      throw new RuntimeException("Couldn't write object to bytebuffer stream!", e);
    }
    return target;
  }

  /**
   * Store stashable instance into the specified target buffer using its own stash method.
   * 
   * @param target
   *          buffer to write to
   * @param object
   *          the stashable instance to stash, {@code null} is permitted
   * @return target buffer
   */
  static ByteBuffer stash(ByteBuffer target, Stashable stashable) {
    assert stashable == null || check(stashable.getClass());
    return stash(target, stashable, stashable::stash);
  }

  /**
   * Store all data needed for spawing this object.
   * 
   * @param target
   *          the buffer to write to
   * @return the target buffer written to
   */
  ByteBuffer stash(ByteBuffer target);

}
