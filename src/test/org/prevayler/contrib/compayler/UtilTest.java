package org.prevayler.contrib.compayler;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class UtilTest {

  @Test
  public void testIndex() throws Exception {
    assertEquals(-1, Util.index(Object.class.getDeclaredMethod("equals", Object.class), Deprecated.class));
    assertEquals(-1, Util.index(UtilTest.class.getDeclaredMethod("testIndex", Date.class), PrevalentDate.class));
    assertEquals(0, Util.index(UtilTest.class.getDeclaredMethod("testIndex0", Date.class, Date.class), PrevalentDate.class));
    assertEquals(1, Util.index(UtilTest.class.getDeclaredMethod("testIndex1", Date.class, Date.class), PrevalentDate.class));
  }

  @SuppressWarnings("unused")
  private void testIndex(Date date) {
    // empty
  }

  @SuppressWarnings("unused")
  private void testIndex0(@PrevalentDate Date date0, Date date1) {
    // empty
  }

  @SuppressWarnings("unused")
  private void testIndex1(Date date0, @PrevalentDate Date date1) {
    // empty
  }

  @Test
  public void testName() {
    assertEquals("Object", Util.name(java.lang.Object.class));
    assertEquals("Class", Util.name(java.lang.Class.class));
    assertEquals("Comparable", Util.name(java.lang.Comparable.class));
    assertEquals("java.lang.annotation.Annotation", Util.name(java.lang.annotation.Annotation.class));
    assertEquals("java.util.Date", Util.name(java.util.Date.class));
  }

  @Test
  public void testWrap() {
    // primitives
    assertSame(Boolean.class, Util.wrap(boolean.class));
    assertSame(Byte.class, Util.wrap(byte.class));
    assertSame(Character.class, Util.wrap(char.class));
    assertSame(Double.class, Util.wrap(double.class));
    assertSame(Float.class, Util.wrap(float.class));
    assertSame(Integer.class, Util.wrap(int.class));
    assertSame(Long.class, Util.wrap(long.class));
    assertSame(Short.class, Util.wrap(short.class));
    assertSame(Void.class, Util.wrap(void.class));
    // non primitive
    assertSame(Object.class, Util.wrap(Object.class));
    assertSame(Class.class, Util.wrap(Class.class));
    assertSame(Number.class, Util.wrap(Number.class));
  }

}
