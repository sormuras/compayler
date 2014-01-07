package com.github.sormuras.compayler;

import static org.junit.Assert.assertEquals;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.sormuras.compayler.Compayler.Configuration;

public class CompaylerTest {

  @SuppressWarnings("serial")
  public static class TestableImplementation extends LinkedList<String> implements Testable<String> {

    @Override
    public Testable<String> direct() {
      return this;
    }

    @Override
    public long executionTime(Date time) {
      return time.getTime();
    }

    @Override
    public Date executionTime(Date seed, Date time, Date... dates) {
      return time;
    }

    @Override
    public Map<String, Date> zzz(List<Map<Integer, Number>> list, int... numbers) throws IllegalStateException, IOException {
      return new HashMap<>(numbers.length);
    }

  }

  @Test
  public void testAppendable() throws Exception {
    Configuration configuration = new Configuration("java.lang.Appendable");

    Scribe scribe = new Scribe(configuration);
    Parser factory = new Parser(configuration);
    String base = "http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/7-b147/";
    factory.getJavaProjectBuilder().addSource(URI.create(base + "java/lang/Appendable.java/?v=source").toURL());
    List<Description> descriptions = factory.createDescriptions();

    Source source = scribe.writeDecorator(descriptions);
    System.out.println(source.getCharContent(true));
  }

  public void testAppendable(Appendable appendable) throws Exception {
    appendable.append('a').append("b").append("abc", 2, 3);
    assertEquals("abc", appendable.toString());
    if (appendable instanceof Closeable) {
      ((Closeable) appendable).close();
    }
  }

  @Test
  public void testAppendableGenerated() throws Exception {
    testAppendable(new StringBuilder());
    // testAppendable(new AppendableDecorator(PrevaylerFactory.createTransientPrevayler(new StringBuilder())));
  }

  @Test
  public void testConfigurationConstructor() {
    Configuration configuration = new Configuration("java.lang.Appendable");
    assertEquals("java.lang.Appendable", configuration.getInterfaceName());
    assertEquals("appendable", configuration.getTargetPackage());
    assertEquals("AppendableDecorator", configuration.getDecoratorName());
  }

  @Test
  public void testTestable() throws Exception {
    Configuration configuration = new Configuration("com.github.sormuras.compayler.Testable");

    Parser parser = new Parser(configuration);
    String base = "http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/7-b147/";
    parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Collection.java/?v=source").toURL());
    parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/List.java/?v=source").toURL());
    parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Queue.java/?v=source").toURL());
    parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Deque.java/?v=source").toURL());
    parser.getJavaProjectBuilder().addSource(new File("src/test/" + configuration.getInterfaceName().replace('.', '/') + ".java"));

    List<Description> descriptions = parser.createDescriptions();
    Scribe scribe = new Scribe(configuration);
    Source source = scribe.writeDecorator(descriptions);
    System.out.println(source.getCharContent(true));
  }

  // @Test
  // public void testTestableGenerated() throws Exception {
  // Testable i = new TestableImplementation();
  // Prevayler<Testable> p = PrevaylerFactory.createTransientPrevayler(i);
  // Testable d = new TestableDecorator(p);
  //
  // assertSame(d, d.direct());
  // assertNotEquals(0l, d.executionTime(new Date(0l)));
  // }

}
