package org.prevayler.contrib.p8.ported;

import java.io.File;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.P8;

public class TransactionWithQueryTest extends FileIOBase {

  @Test
  public void testJavaJournal() throws Exception {
    startAndCrash();
    recover();
  }

  private void startAndCrash() throws Exception {
    Prevayler<StringBuffer> prevayler = createPrevayler();

    assertEquals("the system first", prevayler.execute(new AppendTransactionWithQuery(" first")));
    assertEquals("the system first second", prevayler.execute(new AppendTransactionWithQuery(" second")));
    assertEquals("the system first second third", prevayler.execute(new AppendTransactionWithQuery(" third")));
    assertEquals("the system first second third", prevayler.prevalentSystem().toString());
    prevayler.close();
  }

  private void recover() throws Exception {
    Prevayler<StringBuffer> prevayler = createPrevayler();
    assertEquals("the system first second third", prevayler.prevalentSystem().toString());
    prevayler.close();
  }

  private Prevayler<StringBuffer> createPrevayler() throws Exception {
   
    StringBuffer system = new StringBuffer("the system");
    File base = new File(_testDirectory);
    return new P8<>(system, base);
  }

}
