package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SourceTest {
  
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testCompilation() throws Exception {
    Long expected = 1701L;
    List<String> code = new ArrayList<>();
    code.add("package test;");
    code.add("");
    code.add("import java.util.concurrent.Callable;");
    code.add("");
    code.add("public class Test implements Callable<Long> {");
    code.add("  @Override");
    code.add("  public Long call() {");
    code.add("    return Long.valueOf(" + expected + ");");
    code.add("  }");
    code.add("}");
    Type type = new Type("test.Test");
    Source source = new Source(type.toURI(), code);
    ClassLoader loader = source.compile();
    @SuppressWarnings("unchecked")
    Callable<Long> test = (Callable<Long>) loader.loadClass("test.Test").newInstance();
    assertEquals(expected, test.call());
    Path path = source.save(temp.newFolder().getAbsolutePath());
    assertEquals(code, Files.readAllLines(path, source.getCharset()));
  }

}
