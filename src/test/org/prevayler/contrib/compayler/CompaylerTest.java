package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.prevayler.contrib.compayler.TestTool.decorate;

import java.util.Observable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.contrib.compayler.Compayler.Decorate;

public class CompaylerTest {

  @Decorate(superClass = Observable.class, value = "com.back.sun.Sidec")
  public interface Simple {

    int add(int amount);

    int getSum();

  }

  private class SimpleImpl implements Simple {

    int sum;

    @Override
    public int add(int amount) {
      return sum += amount;
    }

    @Override
    public int getSum() {
      return sum;
    }

  }

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testAppendableDecoratorName() throws Exception {
    Compayler compayler = new Compayler(Appendable.class);
    assertEquals("appendable.AppendableDecorator", compayler.getDecoratorName());
    assertEquals(Object.class, compayler.getSuperClass());
  }

  @Test
  public void testSimpleDecoratorName() throws Exception {
    Compayler compayler = new Compayler(Simple.class);
    assertEquals("com.back.sun.Sidec", compayler.getDecoratorName());
    assertEquals(Observable.class, compayler.getSuperClass());
  }

  @Test
  public void testSimpleDecoratorNew() throws Exception {
    assumeTrue("Ant not running.", Boolean.getBoolean("ant.running")); // quit if NOT inside ant/junit execution
    Simple simple = decorate(Simple.class, new SimpleImpl(), temp.newFolder());
    assertNotNull(simple);
    assertTrue(simple instanceof Observable);
    assertEquals(0, simple.getSum());
    assertEquals(123, simple.add(123));
    assertEquals(123, simple.getSum());
  }

}
