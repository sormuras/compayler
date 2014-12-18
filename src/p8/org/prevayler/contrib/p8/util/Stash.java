package org.prevayler.contrib.p8.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.prevayler.contrib.p8.util.Stashable.Constructor;

/**
 * Stashable store.
 */
public class Stash implements Closeable {

  @SuppressWarnings("unchecked")
  public static class Entry<S extends Stashable> implements Stashable {

    private static Class<?> load(String name, ClassLoader loader) {
      try {
        return Class.forName(name, true, loader);
      } catch (Exception e) {
        throw new Error(e);
      }
    }

    private final transient Constructor<S> constructor;
    private final int slot;
    private final Class<S> type;

    public Entry(ByteBuffer source) {
      this(source, Stash.class.getClassLoader());
    }

    public Entry(ByteBuffer source, ClassLoader loader) {
      this(Nio.getInt7(source), (Class<S>) load(Nio.getString(source), loader));
    }

    public Entry(int slot, Class<S> type) {
      this.slot = slot;
      this.type = type;
      this.constructor = Stashable.constructor(type);
    }

    public Constructor<S> constructor() {
      return constructor;
    }

    public int slot() {
      return slot;
    }

    public Class<S> type() {
      return type;
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      Nio.putInt7(target, slot);
      Nio.putString(target, type.getTypeName());
      return target;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Entry [slot=");
      builder.append(slot);
      builder.append(", type=");
      builder.append(type);
      builder.append(", constructor=");
      builder.append(constructor);
      builder.append("]");
      return builder.toString();
    }

  }

  public interface Realm extends Closeable {

    @Override
    void close() throws IOException;

    void handle(long current, long total, long time, Stashable stashable);

    default ClassLoader loader() {
      return Stash.class.getClassLoader();
    }

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static class Table extends ClassValue<Entry<?>> {

    private final ByteBuffer buffer;

    private final Entry[] entries;

    private final AtomicInteger nextSlot;

    protected Table(ByteBuffer buffer) {
      this(buffer, 10_000);
    }

    protected Table(ByteBuffer buffer, int length) {
      this.buffer = buffer;
      this.entries = new Entry[length];
      this.nextSlot = new AtomicInteger(0);
    }

    /**
     * <code>Action.class -> Entry[slot = 0, type = Action.class, constructor = Action::new]</code>
     */
    @Override
    protected Entry<?> computeValue(Class<?> type) {
      Stashable.check(type);

      // prevent already "computed" values (by build()) to be re-computed
      for (int i = 0; i < entries.length; i++)
        if (entries[i] != null && entries[i].type == type)
          return entries[i];

      int slot = nextSlot.getAndIncrement();
      assert entries[slot] == null : "Slot #" + slot + " already taken?! " + entries[slot];

      Entry entry = new Entry(slot, type);
      buffer.put((byte) 0);
      entry.stash(buffer);
      entries[slot] = entry;
      return entry;
    }

  }

  public enum State {

    /**
     * Fine est.
     */
    CLOSED,

    /**
     * Needs (re-)build.
     */
    INIT,

    /**
     * Awaiting stash.
     */
    READY,

    /**
     * Awaiting commit.
     */
    STASHED
  }

  /**
   * Amount of all stashables in the stash.
   */
  private long age;

  /**
   * Underlying byte buffer.
   */
  private final ByteBuffer memory;

  /**
   * Realm, environment, configuration ... call-back.
   */
  private final Realm realm;

  /**
   * Stashed bytes.
   */
  private int stashed;

  /**
   * Current stash state.
   */
  private State state;

  /**
   * Internal class value helper.
   */
  private final Table table;

  public Stash(ByteBuffer buffer, Realm realm) {
    this.memory = buffer;
    this.realm = realm;
    this.table = new Table(buffer);
    setState(State.INIT);
    build(realm);
  }

  protected void build(Realm realm) {
    this.age = memory.getLong();

    long index;
    int slot = 0;
    for (index = 0; index < age; index++) {
      int code = memory.get();
      if (code == 0) {
        index--;
        @SuppressWarnings("rawtypes")
        Entry entry = new Entry(memory, realm.loader());
        slot = entry.slot;
        assert table.entries[slot] == null : "Slot #" + slot + " already taken?!";
        table.entries[slot] = entry;
        continue;
      }
      if (code == 1) {
        long time = memory.getLong();
        int id = memory.getInt();
        Stashable stashable = get(id).apply(memory);
        realm.handle(index, age, time, stashable);
        continue;
      }
      throw new IllegalStateException("Unknown control code: " + code);
    }
    assert index == age : "Build failed: counter does not match age!";

    table.nextSlot.set(++slot);
    setState(State.READY);
  }

  protected void checkState(State expected) {
    if (this.state == expected)
      return;
    throw new IllegalStateException("Expected state " + expected.name() + ", but was: " + this.state.name());
  }

  @Override
  public void close() throws IOException {
    if (getState() == State.STASHED)
      commit();
    setState(State.CLOSED);
    realm.close();
  }

  public int commit() {
    checkState(State.STASHED);
    memory.putLong(0, ++age);
    setState(State.READY);
    return stashed;
  }

  /**
   * <code>Action.class -> 0</code>
   */
  protected Entry<?> get(Class<?> type) {
    return table.get(type);
  }

  /**
   * <code>0 -> Action::new</code>
   */
  protected Constructor<?> get(int id) {
    return table.entries[id].constructor();
  }

  public State getState() {
    return state;
  }

  protected void setState(State state) {
    this.state = state;
  }

  public <S extends Stashable> S stash(S stashable, long time) {
    return stash(stashable, time, false);
  }

  public <S extends Stashable> S stash(S stashable, long time, boolean commit) {
    Objects.requireNonNull(stashable, "stashable must not be null!");
    checkState(State.READY);
    try {
      int id = get(stashable.getClass()).slot();
      int begin = memory.position();
      memory.put((byte) 1);
      memory.putLong(time);
      memory.putInt(id);
      int payload = memory.position();
      stashable.stash(memory);
      int end = memory.position();
      stashed = end - begin;

      setState(State.STASHED);
      if (commit)
        commit();

      if (!Stash.class.desiredAssertionStatus())
        return stashable;
      memory.position(payload).limit(end);
      ByteBuffer slice = memory.slice();
      memory.position(end).limit(memory.capacity());
      @SuppressWarnings("unchecked")
      S copy = (S) get(id).apply(slice);
      return copy;
    } catch (BufferOverflowException e) {
      throw new Error("Increase initial stash file size!", e);
    } catch (Exception e) {
      throw new Error("Copy failed!", e);
    }
  }

  public double usage() {
    return 100d - memory.remaining() * 100d / memory.capacity();
  }

}
