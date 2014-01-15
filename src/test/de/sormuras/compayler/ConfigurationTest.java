package de.sormuras.compayler;

import static org.junit.Assert.*;

import org.junit.Test;

import de.sormuras.compayler.Apis.Nested;
import de.sormuras.compayler.Apis.Nested.Deeply;

public class ConfigurationTest {

  @Test
  public void testClassForName() throws Exception {
    Class.forName("de.sormuras.compayler.Parsable");
    Class.forName("de.sormuras.compayler.Apis$Nested");
  }

  @Test
  public void testConstructorWithAppendable() {
    testConstructorWithAppendable(new Configuration("java.lang.Appendable"));
    testConstructorWithAppendable(new Configuration(Appendable.class));
  }

  private void testConstructorWithAppendable(Configuration configuration) {
    assertEquals("java.lang", configuration.getInterfacePackage());
    assertEquals("Appendable", configuration.getInterfaceName());
    assertEquals("java.lang.Appendable", configuration.getInterfaceClassName());
    assertSame(Appendable.class, configuration.getInterfaceClass());
    assertEquals("appendable", configuration.getDecoratorPackage());
    assertEquals("AppendableDecorator", configuration.getDecoratorName());
    assertEquals("appendable.AppendableDecorator", configuration.getDecoratorClassName());
  }

  @Test
  public void testConstructorWithDeeply() {
    testConstructorWithDeeply(new Configuration("de.sormuras.compayler.Apis$Nested$Deeply"));
    testConstructorWithDeeply(new Configuration(Deeply.class));
  }

  private void testConstructorWithDeeply(Configuration configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Apis$Nested$Deeply", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Apis$Nested$Deeply", configuration.getInterfaceClassName());
    assertSame(Deeply.class, configuration.getInterfaceClass());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("ApisNestedDeeplyDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.ApisNestedDeeplyDecorator", configuration.getDecoratorClassName());
  }

  @Test
  public void testConstructorWithNested() {
    testConstructorWithNested(new Configuration("de.sormuras.compayler.Apis$Nested"));
    testConstructorWithNested(new Configuration(Nested.class));
  }

  private void testConstructorWithNested(Configuration configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Apis$Nested", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Apis$Nested", configuration.getInterfaceClassName());
    assertSame(Nested.class, configuration.getInterfaceClass());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("ApisNestedDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.ApisNestedDecorator", configuration.getDecoratorClassName());
  }

  @Test
  public void testConstructorWithParsable() {
    testConstructorWithParsable(new Configuration("de.sormuras.compayler.Parsable"));
    testConstructorWithParsable(new Configuration(Parsable.class));
  }

  private void testConstructorWithParsable(Configuration configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Parsable", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Parsable", configuration.getInterfaceClassName());
    assertSame(Parsable.class, configuration.getInterfaceClass());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("ParsableDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.ParsableDecorator", configuration.getDecoratorClassName());
  }

}
