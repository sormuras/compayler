package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.prevayler.PrevaylerFactory.prevayler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReflectorTest {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testE101AsCompayled() throws Exception {
    Compayler compayler = new Compayler(Appendable.class);
    Appendable e101 = (Appendable) compayler.decorate(loader -> prevayler(new StringBuilder(), loader, temp.newFolder()));
    e101.append('!');
  }

}
