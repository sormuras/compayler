package org.prevayler.contrib.compayler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;

public class SourceTest {

  @Test
  public void testCompilation() throws Exception {
    long expected = 1701L;
    List<String> code = new ArrayList<>();
    code.add("public class Test implements java.util.concurrent.Callable<Long> {");
    code.add("  @Override");
    code.add("  public Long call() {");
    code.add("    return Long.valueOf(" + expected + ");");
    code.add("  }");
    code.add("}");
    Source source = new Source("", "Test", code);
    ClassLoader loader = source.compile();
    @SuppressWarnings("unchecked")
    Callable<Long> test = (Callable<Long>) loader.loadClass("Test").newInstance();
    Assert.assertEquals(expected, test.call().longValue());
  }

}
