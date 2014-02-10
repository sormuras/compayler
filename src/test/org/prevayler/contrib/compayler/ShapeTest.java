package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class ShapeTest {

  private Shape shape(String name) {
    return shape(name, new Type(void.class));
  }
  
  private Shape shape(String name, Type returnType) {
    return new Shape(name, returnType, null, null, true);
  }
  
  private Shape shape(String name, Type returnType, Param... params) {
    return new Shape(name, returnType, Arrays.asList(params), null, true);
  }

  @Test
  public void testToString() {
    assertEquals("public void run()", shape("run").toString());
    assertEquals("public my.Result calc()", shape("calc", new Type("my.Result")).toString());
    Type i = new Type(int.class);
    assertEquals("public int add(int a, int b)", shape("add", i, new Param("a",i), new Param("b", i)).toString());
  }

}
