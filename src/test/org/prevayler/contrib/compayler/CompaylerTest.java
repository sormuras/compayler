package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;

public class CompaylerTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testCompaylerConfiguration() {
    Configuration<Appendable, StringBuilder> configuration = new Configuration<>(Appendable.class, StringBuilder.class);
    assertFalse(configuration.isImmutable());
    configuration.setPackageName("ab.cd");
    configuration.setDecoratorClassName("Deco");

    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(configuration);
    assertFalse(configuration.isImmutable());
    assertFalse(compayler.getConfiguration() == configuration);
    assertTrue(compayler.getConfiguration().isImmutable());

    GenerateSourcesTask<Appendable, StringBuilder> task = compayler.generateSourcesTask();
    Tag<Appendable> a1 = task.getTag("append", char.class).setParameterNames("character").setDirect(true);
    Tag<Appendable> a2 = task.getTag("append", CharSequence.class).setParameterNames("sequence").setType(PrevalentType.QUERY);
    Tag<Appendable> a3 = task.getTag("append", CharSequence.class, int.class, int.class).setParameterNames("csq", "start", "end");

    assertFalse(a1.isUnique());
    assertFalse(a2.isUnique());
    assertFalse(a3.isUnique());

    List<Source> sources = task.call();
    assertNotNull(sources);
    assertEquals(4, sources.size());
    for (Source source : sources) {
      CharSequence content = source.getCharContent(false);
      assertNotNull(content);
      assertEquals("ab.cd", source.getPackageName());
      if (source.getSimpleClassName().equals(a1.getClassName()))
        assertTrue(content.toString().contains("character"));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCompaylerConstructorWithIllegalArgument() {
    new Compayler<>(AbstractList.class, LinkedList.class);
  }

  @Test
  public void testPrevaylerWithCustomClassLoader() throws Exception {
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);

    // Decorator<Appendable, StringBuilder> decorator1 = compayler.decorate(new StringBuilder(), null);
    // Appendable appendable1 = (Appendable) decorator1;
    // appendable1.append("Works transient!");
    // decorator1.close();

    String prevalenceBase = temp.newFolder("PrevaylerWithCustomClassLoader").toString();
    Decorator<Appendable, StringBuilder> decorator2 = compayler.decorate(new StringBuilder(), prevalenceBase);
    Appendable appendable2 = (Appendable) decorator2;
    appendable2.append("Works persistent!");
    decorator2.close();
  }

  @Test
  public void testStringAsCharSequence() throws Exception {
    String string = "123";
    Decorator<CharSequence, String> decorator = new Compayler<>(CharSequence.class, String.class).decorate(string, temp.newFolder());

    Prevayler<String> prevayler = decorator.prevayler();
    assertNotNull(prevayler);
    assertSame(string, prevayler.prevalentSystem());

    CharSequence sequence = (CharSequence) decorator;
    assertEquals('2', sequence.charAt(1));
    assertEquals(3, sequence.length());
    assertEquals("2", sequence.subSequence(1, 2));

    decorator.close();
  }

  @Test
  public void testStringBuilderAsAppendable() throws Exception {
    String prevalenceBase = temp.newFolder("StringBuilderAsAppendable").toString();
    StringBuilder builder = new StringBuilder();
    assertEquals(0, builder.length());
    int oldLength = builder.length();
    ClassLoader loader;
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);
    try (Decorator<Appendable, StringBuilder> decorator = compayler.decorate(builder, prevalenceBase)) {
      oldLength = builder.length();
      Appendable a = (Appendable) decorator;
      Appendable b = a.append('x').append('y').append("-z-", 1, 2).append("123");
      assertSame(a, b);
      assertEquals(oldLength + 6, builder.length());
      loader = decorator.getClass().getClassLoader();
    }

    // now load prevalent system without compayler, just plain prevayler with custom classloader!
    builder.setLength(0);
    PrevaylerFactory<StringBuilder> factory = new PrevaylerFactory<>();
    factory.configurePrevalentSystem(builder);
    factory.configureJournalSerializer(new JavaSerializer(loader));
    factory.configurePrevalenceDirectory(prevalenceBase);
    Prevayler<StringBuilder> prevayler = factory.create();
    assertEquals(oldLength + 6, builder.length());
    prevayler.close();
  }

}
