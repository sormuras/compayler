package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CompaylerTest {

  @Test
  public void testAppendableBackedByStringBuilder() throws Exception {
    Compayler compayler = new Compayler(Simplicissimus.class);
    assertEquals(Simplicissimus.class.getName() + "Decorator", compayler.getDecoratorName());
  }

}
