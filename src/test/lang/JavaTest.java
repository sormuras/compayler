package lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JavaTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

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
    assertEquals("java.util.Map$Entry", Map.Entry.class.getName());
    assertEquals("java.util.Map.Entry", Map.Entry.class.getCanonicalName());
    assertEquals("Entry", Map.Entry.class.getSimpleName());
    int[][][] m3 = new int[1][1][1];
    assertEquals("class [[[I", m3.getClass().toString());
    assertEquals("[[[I", m3.getClass().getName());
    assertEquals("int[][][]", m3.getClass().getCanonicalName());
    assertEquals("int[][][]", m3.getClass().getSimpleName());
    assertEquals(null, m3.getClass().getPackage());
    assertEquals(m3.getClass(), Class.forName("[[[I"));
  }

  @Test
  public void testFiles() throws Exception {
    File base = temp.newFolder(); // new File(System.getProperty("user.dir"));
    URI uri = URI.create("PrevalenceBase/more/depth/123$456.txt");
    File file = new File(base, uri.getPath());
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), Arrays.asList("123"), Charset.defaultCharset());
  }

  @Test
  public void testTypeParameters() throws Exception {
    assertEquals("T", Comparable.class.getTypeParameters()[0].getName());
    assertSame(Object.class, Comparable.class.getTypeParameters()[0].getBounds()[0]);
    assertEquals("T", Field.class.getDeclaredMethod("getAnnotation", Class.class).getTypeParameters()[0].getName());
    assertSame(Annotation.class, Field.class.getDeclaredMethod("getAnnotation", Class.class).getTypeParameters()[0].getBounds()[0]);
  }

}
