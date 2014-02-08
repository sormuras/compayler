package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.prevayler.contrib.compayler.Type;

public class TypeTest {

  private void testEquality(Class<?> type) {
    String binaryName = type.getName();
    assertEquals(new Type(type), new Type(type));
    assertEquals(new Type(type), new Type(binaryName));
    assertEquals(new Type(binaryName), new Type(binaryName));
    assertEquals(new Type(type).hashCode(), new Type(binaryName).hashCode());
  }

  @Test
  public void testEquality() {
    testEquality(void.class);
    testEquality(Thread.State.class);
    testEquality(int.class);
  }

  @Test
  public void testBinaryName() {
    assertEquals("void", new Type(void.class).getBinaryName());
    assertEquals("void", new Type("void").getBinaryName());
    assertEquals("int", new Type(int.class).getBinaryName());
    assertEquals("int", new Type("int").getBinaryName());
    assertEquals("java.lang.Thread$State", new Type(Thread.State.class).getBinaryName());
    assertEquals("java.util.concurrent.TimeUnit", new Type(TimeUnit.class).getBinaryName());
  }

}
