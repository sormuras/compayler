package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ParamTest {

  @Test
  public void testToString() {
    Param param = new Param();
    param.setName("p");
    param.setType(new Type(int[].class));
    assertEquals("int[] p", param.toString());
    param.setVariable(true);
    assertEquals("int... p", param.toString());
    param.setType(new Type(int[][][][][].class));
    assertEquals("int[][][][]... p", param.toString());
    param.setVariable(false);
    assertEquals("int[][][][][] p", param.toString());
  }

}
