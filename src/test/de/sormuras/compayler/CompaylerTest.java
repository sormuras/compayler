package de.sormuras.compayler;

import static org.junit.Assert.*;

import org.junit.Test;

import de.sormuras.compayler.Apis.Nested;
import de.sormuras.compayler.Apis.Nested.Deeply;

public class CompaylerTest {

  @Test
  public void testClassForName() throws Exception {
    Class.forName("de.sormuras.compayler.Parsable");
    Class.forName("de.sormuras.compayler.Apis$Nested");
  }

  @Test
  public void testConstructorWithAppendable() {
    testConstructorWithAppendable(new Compayler("java.lang.Appendable"));
    testConstructorWithAppendable(new Compayler(Appendable.class));
  }

  private void testConstructorWithAppendable(Compayler configuration) {
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
    testConstructorWithDeeply(new Compayler("de.sormuras.compayler.Apis$Nested$Deeply"));
    testConstructorWithDeeply(new Compayler(Deeply.class));
  }

  private void testConstructorWithDeeply(Compayler configuration) {
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
    testConstructorWithNested(new Compayler("de.sormuras.compayler.Apis$Nested"));
    testConstructorWithNested(new Compayler(Nested.class));
  }

  private void testConstructorWithNested(Compayler configuration) {
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
    testConstructorWithParsable(new Compayler("de.sormuras.compayler.Parsable"));
    testConstructorWithParsable(new Compayler(Parsable.class));
  }

  private void testConstructorWithParsable(Compayler configuration) {
    assertEquals("de.sormuras.compayler", configuration.getInterfacePackage());
    assertEquals("Parsable", configuration.getInterfaceName());
    assertEquals("de.sormuras.compayler.Parsable", configuration.getInterfaceClassName());
    assertSame(Parsable.class, configuration.getInterfaceClass());
    assertEquals("de.sormuras.compayler", configuration.getDecoratorPackage());
    assertEquals("ParsableDecorator", configuration.getDecoratorName());
    assertEquals("de.sormuras.compayler.ParsableDecorator", configuration.getDecoratorClassName());
  }

}
