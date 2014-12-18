package org.prevayler.contrib.p8.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

/**
 * @author Christian Stein
 */
public class StashableTest {

  public static class Alpha implements Stashable {

    boolean bol;
    byte byt;
    char chr;
    double dbl;
    float flt;
    int inr;
    long lng;
    short srt;

    protected Alpha(ByteBuffer source) {
      this.bol = source.get() == 1;
      this.byt = source.get();
      this.chr = source.getChar();
      this.dbl = source.getDouble();
      this.flt = source.getFloat();
      this.inr = source.getInt();
      this.lng = source.getLong();
      this.srt = source.getShort();
    }

    public Alpha(int i) {
      this.bol = i < 1 ? false : true;
      this.byt = (byte) i;
      this.chr = (char) i;
      this.dbl = i;
      this.flt = i;
      this.inr = i;
      this.lng = i;
      this.srt = (short) i;
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      target.put((byte) (bol ? 1 : 0));
      target.put(byt);
      target.putChar(chr);
      target.putDouble(dbl);
      target.putFloat(flt);
      target.putInt(inr);
      target.putLong(lng);
      target.putShort(srt);
      return target;
    }

  }

  public static class Beta extends Alpha {

    public Beta(ByteBuffer source) {
      this(0);
      this.bol = Nio.getBoolean(source);
      this.byt = source.get();
      this.chr = source.getChar();
      this.dbl = source.getDouble();
      this.flt = source.getFloat();
      this.inr = Nio.getInt7(source);
      this.lng = Nio.getLong7(source);
      this.srt = source.getShort();
    }

    public Beta(int i) {
      super(i);
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      Nio.putBoolean(target, bol);
      target.put(byt);
      target.putChar(chr);
      target.putDouble(dbl);
      target.putFloat(flt);
      Nio.putInt7(target, inr);
      Nio.putLong7(target, lng);
      target.putShort(srt);
      return target;
    }

  }

  @SuppressWarnings("serial")
  public static class Data implements Stashable, Serializable {

    private final Alpha alpha;
    private final int i;
    private final Data other;
    private final List<String> strings;
    private final Double zzz;

    @SuppressWarnings("unchecked")
    public Data(ByteBuffer source) {
      this.i = source.getInt();
      this.alpha = new Alpha(source);
      this.other = Stashable.spawn(source, Data::new);
      this.strings = Stashable.spawn(source, List.class);
      this.zzz = Stashable.spawn(source, (Function<ByteBuffer, Double>) (s) -> Double.valueOf(s.getDouble()));
    }

    public Data(int i, Data other, String... strings) {
      this.alpha = new Alpha(i);
      this.i = i;
      this.other = other;
      this.strings = Arrays.asList(strings);
      this.zzz = Double.valueOf(i);
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      target.putInt(i);
      alpha.stash(target);
      Stashable.stash(target, other);
      Stashable.stash(target, strings);
      Stashable.stash(target, target::putDouble, zzz);
      return target;
    }

  }

  static class MissingConstructor implements Stashable {

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      return target;
    }

  }

  static class WorkingWithoutData implements Stashable {

    public WorkingWithoutData() {
      // no data to initialize
    }

    public WorkingWithoutData(ByteBuffer source) {
      // no data to read
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      return target;
    }

  }

  @Test
  public void testAlpha() {
    Stashable.check(Alpha.class);
    Alpha alpha = new Alpha(0);
    ByteBuffer buffer = ByteBuffer.allocate(30);
    assertEquals(30, buffer.remaining());
    alpha.stash(buffer);
    assertEquals(0, buffer.remaining());
    buffer.flip();
    assertEquals(30, buffer.remaining());
    Alpha a2 = new Alpha(buffer);
    assertEquals(alpha.inr, a2.inr);
  }

  @Test
  public void testBeta() {
    Stashable.check(Beta.class);
    Beta beta = new Beta(127);
    ByteBuffer buffer = ByteBuffer.allocate(20);
    assertEquals(20, buffer.remaining());
    beta.stash(buffer);
    assertEquals(0, buffer.remaining());
    buffer.flip();
    assertEquals(20, buffer.remaining());
    Beta b2 = new Beta(buffer);
    assertEquals(beta.inr, b2.inr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckMissingConstructor() {
    Stashable.check(MissingConstructor.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotImplementing() {
    Stashable.check(Object.class);
    Stashable.check(System.class);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNull() {
    Stashable.check(null);
  }

  @Test
  public void testCheckWorkingWithoutData() {
    Stashable.check(WorkingWithoutData.class);
    ByteBuffer buffer = ByteBuffer.allocate(30);
    Stashable.stash(buffer, new WorkingWithoutData());
    assertEquals(1, buffer.position());
    buffer.flip();
    WorkingWithoutData w2 = Stashable.spawn(buffer, WorkingWithoutData::new);
    assertEquals(1, buffer.position());
    assertNotNull(w2);
  }

  @Test
  public void testData() {
    Stashable.check(Data.class);
    Data data = new Data(123, new Data(456, null, "xyz"), "abc", "def");
    ByteBuffer buffer = ByteBuffer.allocate(1000);
    data.stash(buffer);
    buffer.flip();
    Data data2 = new Data(buffer);
    assertEquals(data.i, data2.i);
    assertEquals(data.other.strings, data2.other.strings);
    assertEquals(data.strings, data2.strings);
    assertEquals(data.zzz, data2.zzz);
  }

  @Test
  public void testNullAsValidValue() {
    ByteBuffer target = ByteBuffer.allocate(4);
    Stashable.stash(target, (Object) null);
    Stashable.stash(target, (Stashable) null);
    Stashable.stash(target, target::put, (Byte) null);
    Stashable.stash(target, (Byte) null, target::put);
    assertEquals(4, target.position());
    ByteBuffer source = (ByteBuffer) target.flip();
    assertNull(Stashable.spawn(source, Object.class));
    assertNull(Stashable.spawn(source, Data::new));
    assertNull(Stashable.spawn(source, Byte.class));
    assertNull(Stashable.spawn(source, Byte.class));
    assertEquals(4, source.position());
  }

}
