package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.prevayler.contrib.compayler.Util.brackets;
import static org.prevayler.contrib.compayler.Util.canonical;
import static org.prevayler.contrib.compayler.Util.element;
import static org.prevayler.contrib.compayler.Util.packaged;
import static org.prevayler.contrib.compayler.Util.simple;
import static org.prevayler.contrib.compayler.Util.wrap;

import org.junit.Test;

public class UtilTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBrackets() {
    assertEquals("", brackets(0, true));
    assertEquals("", brackets(0, false));
    assertEquals("...", brackets(1, true));
    assertEquals("[]", brackets(1, false));
    assertEquals("[]...", brackets(2, true));
    assertEquals("[][]", brackets(2, false));
    assertEquals("[][]...", brackets(3, true));
    assertEquals("[][][]", brackets(3, false));
    assertEquals("[][][]...", brackets(4, true));
    assertEquals("[][][][]", brackets(4, false));
    assertEquals("[][][][]...", brackets(5, true));
    assertEquals("[][][][][]", brackets(5, false));
    assertEquals("[][][][][][][][][]...", brackets(10, true));
    assertEquals("[][][][][][][][][][]", brackets(10, false));
    brackets(-123, false);
  }

  @Test(expected = NullPointerException.class)
  public void testCanonical() {
    assertEquals("void", canonical("void"));
    assertEquals("short", canonical("short"));
    assertEquals("java.util.Map.Entry", canonical("java.util.Map$Entry"));
    assertEquals("java.lang.Object", canonical(Object.class.getName()));
    assertEquals("java.lang.Object[][][]", canonical(Object[][][].class.getName()));
    assertEquals("java.lang.Object[][][]", canonical("[[[Ljava.lang.Object;"));
    assertEquals("int[][][]", canonical("[[[I"));
    assertEquals("boolean[]", canonical("[Z"));
    canonical(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testElement() {
    assertSame(boolean.class, element('Z'));
    assertSame(byte.class, element('B'));
    assertSame(char.class, element('C'));
    assertSame(double.class, element('D'));
    assertSame(float.class, element('F'));
    assertSame(int.class, element('I'));
    assertSame(long.class, element('J'));
    assertSame(short.class, element('S'));
    element('V');
  }

  @Test
  public void testPackaged() {
    assertEquals("", packaged("JustSomeClass"));
    assertEquals("java", packaged("java.lang"));
    assertEquals("java.lang", packaged("java.lang.Object"));
    assertEquals("java.lang", packaged("java.lang.Object$Place$Holder"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSimple() {
    assertEquals("Object", simple("java.lang.Object"));
    assertEquals("Object[][][]", simple("java.lang.Object[][][]"));
    assertEquals("Object[][][]", simple("java.lang.Object[][][]"));
    assertEquals("Entry", simple("java.util.Map.Entry"));
    simple("java.util.Map$Entry");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrap() {
    assertSame(Boolean.class, wrap("boolean"));
    assertSame(Byte.class, wrap("byte"));
    assertSame(Character.class, wrap("char"));
    assertSame(Double.class, wrap("double"));
    assertSame(Float.class, wrap("float"));
    assertSame(Integer.class, wrap("int"));
    assertSame(Long.class, wrap("long"));
    assertSame(Short.class, wrap("short"));
    assertSame(Void.class, wrap("void"));
    wrap("java.lang.Error");
  }

}
