package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;
import org.prevayler.contrib.compayler.javac.Source;

public class ProcessorTest {

  @Test
  public void testCompilationWithProcessor() throws Exception {
    File file = new File("src/test/org/prevayler/contrib/compayler/Simplicissimus.java");
    List<String> lines = Files.readAllLines(file.toPath());

    Source source = new Source(Simplicissimus.class.getCanonicalName(), lines);
    ClassLoader loader = source.compile(new Processor());
    @SuppressWarnings("unchecked")
    Class<Simplicissimus> test = (Class<Simplicissimus>) loader.loadClass(Simplicissimus.class.getName());
    assertTrue(test.isInterface());
    assertEquals(9, test.getMethods().length); // compareTo x2 !
  }

}
