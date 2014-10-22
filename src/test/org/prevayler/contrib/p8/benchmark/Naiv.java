package org.prevayler.contrib.p8.benchmark;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.Transaction;

public class Naiv {
  
  @Rule
  public final TemporaryFolder temp = new TemporaryFolder();

  protected <P> void run(Prevayler<StringBuilder> prevayler) {
    for (int i = 0; i < 1000 * 1000; i++) {
      prevayler.execute((Transaction<StringBuilder>) (builder, date) -> builder.append("1"));
    }
  }
  
}
