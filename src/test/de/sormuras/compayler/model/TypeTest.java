package de.sormuras.compayler.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.sormuras.compayler.Apis;
import de.sormuras.compayler.Apis.Nested.Deeply;

public class TypeTest {

  @Test
  public void testArrayDimension() {
    assertEquals(0, new Type(int.class).getArrayDimension());
    assertEquals(1, new Type(int[].class).getArrayDimension());
    assertEquals(2, new Type(int[][].class).getArrayDimension());
    assertEquals(3, new Type(int[][][].class).getArrayDimension());
  }

  @Test(expected = IllegalStateException.class)
  public void testArrayType() {
    assertEquals(Deeply.class.getName(), new Type(new Deeply[1][1].getClass()).getArrayType().getBinaryName());
    assertEquals("int", new Type(int[][][].class).getArrayType().toString());
    assertEquals("int", new Type(int[][].class).getArrayType().toString());
    assertEquals("int", new Type(int[].class).getArrayType().toString());
    assertEquals("bug", new Type(int.class).getArrayType().toString());
  }

  @Test
  public void testGetPackageName() {
    assertEquals("", new Type(int[].class).getPackageName());
    assertEquals("", new Type(void.class).getPackageName());
    assertEquals("java.lang", new Type(Void.class).getPackageName());
    assertEquals("java.lang", new Type(Appendable.class).getPackageName());
    assertEquals("java.util", new Type(new ArrayList<Integer>().getClass()).getPackageName());
    assertEquals("java.util", new Type(List.class, "<java.lang.Integer>").getPackageName());
    assertEquals("de.sormuras.compayler", new Type(Deeply.class).getPackageName());
  }

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
    assertEquals("int[][][]", new Type(m3.getClass()).toString());
    assertEquals("java.lang.Integer", new Type(Integer.class).toString());
    assertEquals("java.util.HashMap", new Type(new HashMap<String, String>().getClass()).toString());
    assertEquals("java.lang.Object[]", new Type(Object[].class).toString());
    assertEquals("void", new Type(void.class).toString());
    assertEquals("java.lang.Void", new Type(Void.class).toString());
    assertEquals("java.lang.Appendable", new Type(Appendable.class).toString());
    assertEquals("java.util.ArrayList", new Type(new ArrayList<Integer>().getClass()).toString());
    assertEquals("java.util.List<java.lang.Integer>", new Type(List.class, "<java.lang.Integer>").toString());

    assertEquals("java.util.HashMap<String, String>", new Type("java.util.HashMap", "<String, String>").toString());
    assertEquals("java.util.HashMap[][]", new Type(HashMap[][].class).toString());
    assertEquals("[[Ljava.util.HashMap;", new Type(HashMap[][].class).getBinaryName());
    assertEquals("java.util.HashMap[][]", new Type("[[Ljava.util.HashMap;").toString());
    assertEquals("[[Ljava.util.HashMap;", new Type("[[Ljava.util.HashMap;").getBinaryName());
    assertEquals("java.lang.Object[]", new Type("[Ljava.lang.Object;").toString());
  }

  @Test
  public void testVariableArgumentDimension() throws Exception {
    assertEquals(1, new Type(Object[].class).getArrayDimension());
    Class<?>[] types = Apis.Nested.Deeply.class.getDeclaredMethod("variable", Object[].class).getParameterTypes();
    assertEquals(1, new Type(types[types.length - 1]).getArrayDimension());
  }

}
