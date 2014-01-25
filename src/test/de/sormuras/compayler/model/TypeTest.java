package de.sormuras.compayler.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class TypeTest {

  @Test
  public void testBrackets() {
    assertEquals("", Type.brackets(0, true));
    assertEquals("", Type.brackets(0, false));
    assertEquals("...", Type.brackets(1, true));
    assertEquals("[]", Type.brackets(1, false));
    assertEquals("[]...", Type.brackets(2, true));
    assertEquals("[][]", Type.brackets(2, false));
    assertEquals("[][]...", Type.brackets(3, true));
    assertEquals("[][][]", Type.brackets(3, false));
    assertEquals("[][][]...", Type.brackets(4, true));
    assertEquals("[][][][]", Type.brackets(4, false));
    assertEquals("[][][][]...", Type.brackets(5, true));
    assertEquals("[][][][][]", Type.brackets(5, false));
  }

  @Test
  public void testToString() {
    int[][][] m3 = new int[1][1][1];
    assertEquals("int[][][]", Type.forName(m3.getClass().getCanonicalName()).toString());
    assertEquals("java.lang.Integer", Type.forName(new Integer(3).getClass().getCanonicalName()).toString());
    assertEquals("java.util.HashMap", Type.forName(new HashMap<String, String>().getClass().getCanonicalName()).toString());
    assertEquals("java.util.HashMap<String, String>", Type.forName("java.util.HashMap", "<String, String>").toString());
    assertEquals("java.util.HashMap[][]", Type.forName("java.util.HashMap", 2).toString());
  }

}
