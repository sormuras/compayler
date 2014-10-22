package org.prevayler.contrib.p8.benchmark;

import org.junit.Test;
import org.prevayler.contrib.p8.P8;

public class BenchP8 extends Naiv {

  @Test
  public void runWithP8() throws Exception {
    try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), temp.newFolder())) {
      run(prevayler);
    }
  }

}
