package de.sormuras.compayler.model;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class TypeTest {

  @Test
  public void testBrackets() {
    Assert.assertEquals("", Type.brackets(0, true));
    Assert.assertEquals("", Type.brackets(0, false));
    Assert.assertEquals("...", Type.brackets(1, true));
    Assert.assertEquals("[]", Type.brackets(1, false));
    Assert.assertEquals("[]...", Type.brackets(2, true));
    Assert.assertEquals("[][]", Type.brackets(2, false));
    Assert.assertEquals("[][]...", Type.brackets(3, true));
    Assert.assertEquals("[][][]", Type.brackets(3, false));
    Assert.assertEquals("[][][]...", Type.brackets(4, true));
    Assert.assertEquals("[][][][]", Type.brackets(4, false));
    Assert.assertEquals("[][][][]...", Type.brackets(5, true));
    Assert.assertEquals("[][][][][]", Type.brackets(5, false));
  }

  @Test
  public void testToString() {
    int[][][] m3 = new int[1][1][1];
    Assert.assertEquals("int[][][]", Type.forName(m3.getClass().getCanonicalName()).toString());
    Assert.assertEquals("java.lang.Integer", Type.forName(new Integer(3).getClass().getCanonicalName()).toString());
    Assert.assertEquals("java.util.HashMap", Type.forName(new HashMap<String, String>().getClass().getCanonicalName()).toString());
    Assert.assertEquals("java.util.HashMap<String, String>", Type.forName("java.util.HashMap", "<String, String>").toString());
    Assert.assertEquals("java.util.HashMap[][]", Type.forName("java.util.HashMap", 2).toString());
  }

}
