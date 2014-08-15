package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompaylerTest {

  @Test
  public void testDecoratorName() throws Exception {
    Compayler compayler = new Compayler(Simplicissimus.class);
    assertEquals(Simplicissimus.class.getName() + "Decorator", compayler.getDecoratorName());
  }
  
  
  @Test
  public void testClassForName() throws Exception {
    assertEquals("org.prevayler.contrib.compayler.Simplicissimus", Simplicissimus.class.getName());
    assertEquals("org.prevayler.contrib.compayler.Simplicissimus", Simplicissimus.class.getCanonicalName());
    assertEquals(Simplicissimus.class, Class.forName(Simplicissimus.class.getName()));
  }

}
