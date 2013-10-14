package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;

public class CompaylerTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testCompaylerConfiguration() {
    Configuration<Appendable> configuration = new Configuration<>(Appendable.class);
    configuration.setPackageName("ab.cd");
    configuration.setDecoratorClassName("Deco");
    configuration.setPrevalentSystemClass(StringBuilder.class);
    
    Compayler<Appendable> compayler = new Compayler<>(configuration);
    GenerateSourcesTask<Appendable> generateSourcesTask = compayler.generateSourcesTask();
    String a1 = generateSourcesTask.getTag("append", char.class).setParameterNames("character").setDirect(true).getClassName();
    generateSourcesTask.getTag("append", CharSequence.class).setParameterNames("sequence").setType(PrevalentType.QUERY);
    generateSourcesTask.getTag("append", CharSequence.class, int.class, int.class).setParameterNames("csq", "start", "end");

    List<Source> sources = generateSourcesTask.call();
    assertNotNull(sources);
    assertEquals(4, sources.size());
    for (Source source : sources) {
      CharSequence content = source.getCharContent(false);
      assertNotNull(content);
      assertEquals("ab.cd", source.getPackageName());
      if (source.getSimpleClassName().equals(a1))
        assertTrue(content.toString().contains("character"));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCompaylerConstructorWithIllegalArgument() {
    new Compayler<>(String.class);
  }

  @Test
  public void testCompaylerForCharSequence() throws Exception {
    CharSequence prevalentSystem = "123";
    PrevaylerCreator<CharSequence> creator = new PrevaylerCreator.TransientPrevaylerCreator<>(prevalentSystem);
    PrevaylerDecorator<CharSequence> decorator = new Compayler<>(CharSequence.class).toDecorator(creator);

    Prevayler<CharSequence> prevayler = decorator.prevayler();
    assertNotNull(prevayler);
    assertSame(prevalentSystem, prevayler.prevalentSystem());

    CharSequence sequence = (CharSequence) decorator;
    assertEquals('2', sequence.charAt(1));
    assertEquals(3, sequence.length());
    assertEquals("2", sequence.subSequence(1, 2));
  }

  @Test
  public void testStringBuilderAsAppendable() throws Exception {
    final String prevalenceBase = temporaryFolder.newFolder("testStringBuilderAsAppendable").toString();
    final StringBuilder builder = new StringBuilder();
    assertEquals(0, builder.length());

    PrevaylerCreator<Appendable> creator = new PrevaylerCreator.DefaultPrevaylerCreator<>((Appendable) builder, prevalenceBase);
    PrevaylerDecorator<Appendable> decorator = new Compayler<>(Appendable.class).toDecorator(creator);

    int oldLength = builder.length();
    Appendable a = (Appendable) decorator;
    Appendable b = a.append('x').append('y').append("-z-", 1, 2).append("123");
    assertSame(a, b);
    assertEquals(oldLength + 6, builder.length());
    decorator.prevayler().close();

    // now load prevalent system without compayler, just plain prevayler!
    builder.setLength(0);
    Prevayler<StringBuilder> prevayler = PrevaylerFactory.createPrevayler(builder, prevalenceBase);
    assertEquals(oldLength + 6, builder.length());
    prevayler.close();
  }

}
