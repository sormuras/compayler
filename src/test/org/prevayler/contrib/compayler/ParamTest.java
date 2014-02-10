package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.prevayler.contrib.compayler.Util.merge;

import org.junit.Test;

public class ParamTest {

  @Test
  public void testMerged() {
    Param oracle = new Param();
    oracle.setName("oracle");
    oracle.setType(new Type(Boolean.class));
    Param integers = new Param();
    integers.setName("integers");
    integers.setType(new Type(int[].class));
    integers.setVariable(true);
    assertEquals("(java.lang.Boolean oracle, int... integers)", merge("(", ")", ", ", oracle, integers));
  }
  
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
