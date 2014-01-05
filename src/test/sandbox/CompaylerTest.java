package sandbox;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import sandbox.Compayler.Configuration;
import sandbox.Compayler.DescriptionVisitor;
import sandbox.Compayler.DescriptionWriter;

public class CompaylerTest {

  public static class TestableImplementation implements Nestable {

    @Override
    public Nestable direct() {
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
    // System.out.println(configuration);

    Scribe scribe = new Scribe(configuration);

    // DescriptionFactory factory = scribe;
    Parser factory = new Parser(configuration);
    factory.getJavaProjectBuilder().addSource(
        URI.create("http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/7-b147/java/lang/Appendable.java/?v=source")
            .toURL());
    List<Description> descriptions = factory.createDescriptions();

    DescriptionVisitor visitor = scribe;
    visitor.visitDescriptions(descriptions);

    DescriptionWriter writer = scribe;

    Source source = writer.writeDecorator(descriptions);
    System.out.println(source.getCharContent(true));
  }

  public void testAppendable(Appendable appendable) throws Exception {
    appendable.append('a').append("b").append("abc", 2, 3);
  }

  @Test
  public void testAppendableGenerated() throws Exception {
    // pojo
    Appendable appendable = new StringBuilder();
    testAppendable(appendable);
    assertEquals("abc", appendable.toString());

    // decorated
    // appendable = new StringBuilder();
    // Prevayler<Appendable> prevayler = PrevaylerFactory.createTransientPrevayler(appendable);
    // try (AppendableDecorator decorator = new AppendableDecorator(prevayler)) {
    // testAppendable(decorator);
    // }
    // assertEquals("abc", appendable.toString());
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
    Configuration configuration = new Configuration("sandbox.Nestable");

    Parser parser = new Parser(configuration);
    parser.getJavaProjectBuilder().addSource(new File("src/test/" + configuration.getInterfaceName().replace('.', '/') + ".java"));

    List<Description> descriptions = parser.createDescriptions();
    Scribe scribe = new Scribe(configuration);
    scribe.visitDescriptions(descriptions);
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
