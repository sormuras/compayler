package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.prevayler.contrib.compayler.prevayler.PrevaylerFactory.prevayler;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.contrib.compayler.javac.Source;

public class ProcessorTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @SuppressWarnings("unchecked")
  @Test
  public void testCompilationWithProcessor() throws Exception {
    File file = new File("src/test/org/prevayler/contrib/compayler/Simplicissimus.java");
    List<String> lines = Files.readAllLines(file.toPath());

    Source source = new Source(Simplicissimus.class.getCanonicalName(), lines);
    source.getCompilerProcessors().add(new Processor());
    ClassLoader loader = source.compile();
    Class<Simplicissimus> interfaceClass = (Class<Simplicissimus>) loader.loadClass(Simplicissimus.class.getName());
    assertTrue(interfaceClass.isInterface());
    assertEquals(9, interfaceClass.getMethods().length); // compareTo x2 !
    Class<? extends Simplicissimus> decoratorClass = (Class<? extends Simplicissimus>) loader.loadClass(Simplicissimus.class.getName()
        + "Decorator");
    assertTrue(!decoratorClass.isInterface());
    Simplicissimus s = decoratorClass.getConstructor(Prevayler.class).newInstance(
        prevayler(new CompaylerTest.Simple(), loader, temp.newFolder()));
    assertEquals("123", s.append('1').append("2").append("123", 2, 3).toString());
  }

}
