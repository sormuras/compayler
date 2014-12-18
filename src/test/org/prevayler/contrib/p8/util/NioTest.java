package org.prevayler.contrib.p8.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class NioTest {

  @Test
  public void putAndGetEnum() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    TimeUnit expected = TimeUnit.DAYS;
    buffer.clear();
    Nio.putEnum(buffer, expected);
    buffer.flip();
    Assert.assertSame(expected, Nio.getEnum(TimeUnit.class, buffer));
  }

  @Test
  public void putAndGetInt7() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    assertEquals(1, putAndGetInt7(buffer, 0));
    assertEquals(1, putAndGetInt7(buffer, 1));
    assertEquals(1, putAndGetInt7(buffer, 2));
    assertEquals(1, putAndGetInt7(buffer, 3));
    assertEquals(1, putAndGetInt7(buffer, 127));
    assertEquals(2, putAndGetInt7(buffer, 128));
    assertEquals(2, putAndGetInt7(buffer, 16383));
    assertEquals(3, putAndGetInt7(buffer, 16384));
    assertEquals(3, putAndGetInt7(buffer, 2097151));
    assertEquals(4, putAndGetInt7(buffer, 2097152));
    assertEquals(4, putAndGetInt7(buffer, 268435455));
    assertEquals(5, putAndGetInt7(buffer, 268435456));
    assertEquals(5, putAndGetInt7(buffer, Integer.MAX_VALUE));
  }

  private int putAndGetInt7(ByteBuffer buffer, int probe) {
    buffer.clear();
    Nio.putInt7(buffer, probe);
    buffer.flip();
    assertEquals(probe, Nio.getInt7(buffer));
    return buffer.position();
  }

  @Test
  public void putAndGetLong7() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    // same probes as above by the int7 test
    assertEquals(1, putAndGetLong7(buffer, 0));
    assertEquals(1, putAndGetLong7(buffer, 1));
    assertEquals(1, putAndGetLong7(buffer, 2));
    assertEquals(1, putAndGetLong7(buffer, 3));
    assertEquals(1, putAndGetLong7(buffer, 127));
    assertEquals(2, putAndGetLong7(buffer, 128));
    assertEquals(2, putAndGetLong7(buffer, 16383));
    assertEquals(3, putAndGetLong7(buffer, 16384));
    assertEquals(3, putAndGetLong7(buffer, 2097151));
    assertEquals(4, putAndGetLong7(buffer, 2097152));
    assertEquals(4, putAndGetLong7(buffer, 268435455));
    assertEquals(5, putAndGetLong7(buffer, 268435456));
    assertEquals(5, putAndGetLong7(buffer, Integer.MAX_VALUE));
    // getting higher...
    assertEquals(5, putAndGetLong7(buffer, (1L << 31) - 1)); // Integer.MAX_VALUE
    assertEquals(5, putAndGetLong7(buffer, (1L << 31)));
    assertEquals(5, putAndGetLong7(buffer, (1L << 31) + 1));
    assertEquals(5, putAndGetLong7(buffer, (1L << 35) - 1));
    assertEquals(6, putAndGetLong7(buffer, (1L << 35)));
    assertEquals(6, putAndGetLong7(buffer, (1L << 42) - 1));
    assertEquals(7, putAndGetLong7(buffer, (1L << 42)));
    assertEquals(7, putAndGetLong7(buffer, (1L << 49) - 1));
    assertEquals(8, putAndGetLong7(buffer, (1L << 49)));
    assertEquals(8, putAndGetLong7(buffer, (1L << 56) - 1));
    assertEquals(9, putAndGetLong7(buffer, (1L << 56)));
    assertEquals(9, putAndGetLong7(buffer, Long.MAX_VALUE));
  }

  private int putAndGetLong7(ByteBuffer buffer, long expected) {
    buffer.clear();
    Nio.putLong7(buffer, expected);
    buffer.flip();
    long actual = Nio.getLong7(buffer);
    assertEquals(expected, actual);
    return buffer.position();
  }

  @Test
  public void putAndGetString7() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    Object[][] data = new Object[][] { { buffer, "123" }, { buffer, "??????\tABCxyz��������?��\u7FFF\u8000" } };
    for (Object[] a : data) {
      putAndGetString((ByteBuffer) a[0], (String) a[1]);
    }
  }

  private void putAndGetString(ByteBuffer buffer, String string) {
    buffer.clear();
    Nio.putString(buffer, string);
    buffer.flip();
    String actual = Nio.getString(buffer);
    assertEquals(string, actual);
  }

  @Test
  public void putNegativeArgumentFails() {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    putNegativeArgumentFails(buffer, -1);
    putNegativeArgumentFails(buffer, Byte.MIN_VALUE);
    putNegativeArgumentFails(buffer, Short.MIN_VALUE + 1);
    putNegativeArgumentFails(buffer, Short.MIN_VALUE);
    putNegativeArgumentFails(buffer, Integer.MIN_VALUE);
    putNegativeArgumentFails(buffer, Long.MIN_VALUE);
  }

  private void putNegativeArgumentFails(ByteBuffer buffer, long illegal) {
    try {
      if (illegal >= Integer.MIN_VALUE) { // exclude Long.MIN_VALUE
        Nio.putInt7(buffer, (int) illegal);
        fail();
      }
      Nio.putLong7(buffer, illegal);
      fail();
    } catch (AssertionError | IllegalArgumentException e) {
      // expected
    }
  }

}
