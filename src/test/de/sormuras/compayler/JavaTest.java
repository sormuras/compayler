package de.sormuras.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JavaTest {

  @Test
  public void testArrayOfArrayType() {
    int[][][] m3 = new int[1][1][1];
    assertTrue(m3.getClass().isArray());
    assertTrue(m3.getClass().getComponentType().isArray());
    assertTrue(m3.getClass().getComponentType().getComponentType().isArray());
    assertEquals(int.class, m3.getClass().getComponentType().getComponentType().getComponentType());
  }

  @Test
  public void testAssignable() {
    assertTrue(Number.class.isAssignableFrom(Integer.class));
    assertFalse(Integer.class.isAssignableFrom(Number.class));
  }

  @Test
  public void testClassNames() {
    Class<Apis.Nested.Deeply> deeplyClass = Apis.Nested.Deeply.class;
    assertEquals("interface de.sormuras.compayler.Apis$Nested$Deeply", deeplyClass.toString());
    assertEquals("de.sormuras.compayler.Apis$Nested$Deeply", deeplyClass.getName());
    assertEquals("de.sormuras.compayler.Apis.Nested.Deeply", deeplyClass.getCanonicalName());
    assertEquals("Deeply", deeplyClass.getSimpleName());
    assertEquals("de.sormuras.compayler", deeplyClass.getPackage().getName());
    int[][][] m3 = new int[1][1][1];
    assertEquals("class [[[I", m3.getClass().toString());
    assertEquals("[[[I", m3.getClass().getName());
    assertEquals("int[][][]", m3.getClass().getCanonicalName());
    assertEquals("int[][][]", m3.getClass().getSimpleName());
    assertEquals(null, m3.getClass().getPackage());
  }

}
