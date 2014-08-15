package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.contrib.compayler.Compayler.Decorate;

public class CompaylerTest {

  @Decorate
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
  public void testDecoratorName() throws Exception {
    Compayler compayler = new Compayler(Simple.class);
    assertEquals(Simple.class.getPackage().getName() + "." + Simple.class.getSimpleName() + "Decorator", compayler.getDecoratorName());
  }

  @Test
  public void testDecoratorNew() throws Exception {
    assumeTrue("Ant not running.", Boolean.getBoolean("ant.running")); // quit if NOT inside ant/junit execution
    Simple simple = Compayler.decorate(Simple.class, new SimpleImpl(), temp.newFolder());
    assertNotNull(simple);
    assertEquals(0, simple.getSum());
    assertEquals(123, simple.add(123));
  }

}
