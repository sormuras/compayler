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
import org.prevayler.Prevayler;

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
    public int query() {
      return 0;
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

    @SuppressWarnings("unused")
    Source source = scribe.writeDecorator(descriptions);
    // System.out.println(source.getCharContent(true));
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

    // testAppendable(new AppendableDecorator(new VolatilePrevayler<>(new StringBuilder())));

    ClassLoader loader = Loader.compile(Appendable.class);
    Prevayler<? extends Appendable> prevayler = new VolatilePrevayler<>(new StringBuilder(), loader);
    // Prevayler<? extends Appendable> prevayler = Loader.newPrevayler(new StringBuilder(), loader);
    Class<?> decoratorClass = loader.loadClass("appendable.AppendableDecorator");
    Appendable appendable = (Appendable) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
    testAppendable(appendable);

    testAppendable(decorate(Appendable.class, "appendable.AppendableDecorator", new StringBuilder()));
  }

  @SuppressWarnings("unchecked")
  private <P> P decorate(Class<P> interfaceClass, String decoratorClassName, P prevalentSystem) throws Exception {
    ClassLoader loader = Loader.compile(interfaceClass);
    Prevayler<? extends P> prevayler = new VolatilePrevayler<>(prevalentSystem, loader);
    // Prevayler<? extends P> prevayler = Loader.newPrevayler(prevalentSystem, loader);
    Class<?> decoratorClass = loader.loadClass(decoratorClassName);
    return (P) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  @Test
  public void testConfigurationConstructor() {
    Configuration configuration = new Configuration("java.lang.Appendable");
    assertEquals("java.lang.Appendable", configuration.getInterfaceName());
    assertEquals("appendable", configuration.getTargetPackage());
    assertEquals("AppendableDecorator", configuration.getDecoratorName());
  }

  // @Test
  // public void testTestable() throws Exception {
  // // testTestable(new TestableImplementation()); // ... fails for execution time mismatch
  //
  // Configuration configuration = new Configuration("com.github.sormuras.compayler.Testable");
  // Parser parser = new Parser(configuration);
  // String base = "http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/7-b147/";
  // parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Collection.java/?v=source").toURL());
  // parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/List.java/?v=source").toURL());
  // parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Queue.java/?v=source").toURL());
  // parser.getJavaProjectBuilder().addSource(URI.create(base + "java/util/Deque.java/?v=source").toURL());
  // parser.getJavaProjectBuilder().addSource(new File("src/test/" + configuration.getInterfaceName().replace('.', '/') + ".java"));
  //
  // List<Description> descriptions = parser.createDescriptions();
  // Scribe scribe = new Scribe(configuration);
  // Source source = scribe.writeDecorator(descriptions);
  //
  // ClassLoader loader = Loader.compile(Arrays.asList(source), getClass().getClassLoader());
  //
  // Prevayler<? extends Testable<String>> prevayler = new VolatilePrevayler<>(new TestableImplementation(), loader);
  // Testable<String> testable = Loader.decorate(source, prevayler);
  // testTestable(testable);
  // }
  //
  // public void testTestable(Testable<String> testable) throws Exception {
  // Assert.assertSame(testable, testable.direct());
  // Assert.assertNotEquals(0L, testable.executionTime(new Date(0L)));
  // }

  @Test
  public void testParsable() throws Exception {
    Configuration configuration = new Configuration(Parsable.class.getCanonicalName());
    Parser parser = new Parser(configuration);
    parser.getJavaProjectBuilder().addSource(new File("src/test/" + configuration.getInterfaceName().replace('.', '/') + ".java"));
    List<Description> descriptions = parser.createDescriptions();
    System.out.println(descriptions);
  }

}
