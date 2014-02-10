package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TypeTest {

  @Test
  public void testBinaryName() {
    assertEquals("void", new Type(void.class).getBinaryName());
    assertEquals("void", new Type("void").getBinaryName());
    assertEquals("int", new Type(int.class).getBinaryName());
    assertEquals("int", new Type("int").getBinaryName());
    assertEquals("java.lang.Thread$State", new Type(Thread.State.class).getBinaryName());
    assertEquals("java.util.concurrent.TimeUnit", new Type(TimeUnit.class).getBinaryName());
  }

  @Test
  public void testEquality() {
    testEquality(void.class);
    testEquality(Thread.State.class);
    testEquality(int.class);
  }

  private void testEquality(Class<?> type) {
    String binaryName = type.getName();
    assertEquals(new Type(type), new Type(type));
    assertEquals(new Type(type), new Type(binaryName));
    assertEquals(new Type(binaryName), new Type(binaryName));
    assertEquals(new Type(type).hashCode(), new Type(binaryName).hashCode());
  }


  @Test
  public void testWrap() {
    assertEquals("java.lang.Boolean", new Type(boolean.class).getCanonicalName(true));
    assertEquals("java.lang.Byte", new Type(byte.class).getCanonicalName(true));
    assertEquals("java.lang.Character", new Type(char.class).getCanonicalName(true));
    assertEquals("java.lang.Double", new Type(double.class).getCanonicalName(true));
    assertEquals("java.lang.Float", new Type(float.class).getCanonicalName(true));
    assertEquals("java.lang.Integer", new Type(int.class).getCanonicalName(true));
    assertEquals("java.lang.Long", new Type(long.class).getCanonicalName(true));
    assertEquals("java.lang.Short", new Type(short.class).getCanonicalName(true));
    assertEquals("java.lang.Void", new Type(void.class).getCanonicalName(true));
  }
  
}
