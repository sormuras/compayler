package org.prevayler.contrib.p8.benchmark;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.contrib.compayler.TestTool;

public class BenchDefault extends Naiv {

  @Test
  public void runWithDefaultPrevayler() throws Exception {
    Prevayler<StringBuilder> prevayler = TestTool.prevayler(new StringBuilder(), temp.newFolder());
    try {
      run(prevayler);
    } finally {
      prevayler.close();
    }
  }

}
