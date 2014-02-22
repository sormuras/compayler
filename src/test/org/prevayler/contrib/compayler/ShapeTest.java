package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShapeTest {

  private Shape shape(String name) {
    return shape(name, new Type(void.class));
  }

  private Shape shape(String name, Type returnType) {
    return new Shape(name, returnType, null, null, true);
  }

  @Test
  public void testEquality() {
    assertEquals(shape("run"), shape("run"));
    assertEquals(shape("calc", new Type("java.lang.Integer")), shape("calc", new Type(Integer.class)));
  }

}
