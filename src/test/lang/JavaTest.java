package lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.junit.Test;

import de.sormuras.compayler.Apis;
import de.sormuras.compayler.Parsable;

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
  public void testClassNames() throws ClassNotFoundException {
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

    assertEquals(m3.getClass(), Class.forName("[[[I"));
    assertEquals("[Lde.sormuras.compayler.Apis$Nested$Deeply;", new Apis.Nested.Deeply[1].getClass().getName());
  }

  @Test
  public void testTypeParameters() throws Exception {
    assertEquals("T", Comparable.class.getTypeParameters()[0].getName());
    assertSame(Object.class, Comparable.class.getTypeParameters()[0].getBounds()[0]);
    assertEquals("T", Field.class.getDeclaredMethod("getAnnotation", Class.class).getTypeParameters()[0].getName());
    assertSame(Annotation.class, Field.class.getDeclaredMethod("getAnnotation", Class.class).getTypeParameters()[0].getBounds()[0]);
    assertEquals("E", Parsable.class.getTypeParameters()[0].getName());
    assertSame(Object.class, Parsable.class.getTypeParameters()[0].getBounds()[0]);
    assertEquals("T", Parsable.class.getTypeParameters()[1].getName());
    assertEquals("E", Parsable.class.getTypeParameters()[1].getBounds()[0].toString());
  }

}
