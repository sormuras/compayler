package de.sormuras.compayler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.sormuras.compayler.Apis.Nested;

public class ConfigurationTest {

  @Test
  public void testClassForName() throws Exception {
    Class.forName("de.sormuras.compayler.Parsable");
    Class.forName("de.sormuras.compayler.Apis$Nested");
  }

  @Test
  public void testConstructor() {
    testConstructorWithAppendable(new Configuration("java.lang.Appendable"));
    testConstructorWithAppendable(new Configuration(Appendable.class));
    testConstructorWithParsable(new Configuration("de.sormuras.compayler.Parsable"));
    testConstructorWithParsable(new Configuration(Parsable.class));
    testConstructorWithNested(new Configuration("de.sormuras.compayler.Apis$Nested"));
    testConstructorWithNested(new Configuration(Nested.class));
  }

  private void testConstructorWithAppendable(Configuration configuration) {
    assertEquals("java.lang", configuration.getInterfacePackage());
    assertEquals("Appendable", configuration.getInterfaceName());
    assertEquals("java.lang.Appendable", configuration.getInterfaceClassName());
    assertEquals("appendable", configuration.getDecoratorPackage());
    assertEquals("AppendableDecorator", configuration.getDecoratorName());
    assertEquals("appendable.AppendableDecorator", configuration.getDecoratorClassName());
  }

  private void testConstructorWithNested(Configuration configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Apis$Nested", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Apis$Nested", configuration.getInterfaceClassName());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("Apis$NestedDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.Apis$NestedDecorator", configuration.getDecoratorClassName());
  }

  private void testConstructorWithParsable(Configuration configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Parsable", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Parsable", configuration.getInterfaceClassName());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("ParsableDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.ParsableDecorator", configuration.getDecoratorClassName());
  }

}
