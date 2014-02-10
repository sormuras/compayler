package org.prevayler.contrib.compayler;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SourceTest {

  @Test
  public void testCompilation() throws Exception {
    long expected = 1701L;
    List<String> code = new ArrayList<>();
    code.add("public class Test {");
    code.add("  public long test() {");
    code.add(format("    return %d;", expected));
    code.add("  }");
    code.add("}");
    Source source = new Source("", "Test", code);
    ClassLoader loader = source.compile();
    Object test = loader.loadClass("Test").newInstance();
    Assert.assertEquals(expected, (long) test.getClass().getDeclaredMethod("test").invoke(test));
  }

}
