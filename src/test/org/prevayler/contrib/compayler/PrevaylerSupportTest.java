package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.TransactionWithQuery;

import com.github.sormuras.compayler.VolatilePrevayler;

public class PrevaylerSupportTest {

  private interface AppendableExecutable {

    class AppendChar implements TransactionWithQuery<Appendable, Appendable> {

      private static final long serialVersionUID = 1L;

      private final char c;

      private AppendChar(char c) {
        this.c = c;
      }

      @Override
      public Appendable executeAndQuery(Appendable prevalentSystem, Date executionTime) throws Exception {
        return prevalentSystem.append(c);
      }

    }

    class AppendCharSequence implements TransactionWithQuery<Appendable, Appendable> {

      private static final long serialVersionUID = 1L;

      private final CharSequence csq;

      private AppendCharSequence(CharSequence csq) {
        this.csq = csq;
      }

      @Override
      public Appendable executeAndQuery(Appendable prevalentSystem, Date executionTime) throws Exception {
        return prevalentSystem.append(csq);
      }

    }

    class AppendCharSequenceStartEnd implements TransactionWithQuery<Appendable, Appendable> {

      private static final long serialVersionUID = 1L;

      private final CharSequence csq;
      private final int start, end;

      private AppendCharSequenceStartEnd(CharSequence csq, int start, int end) {
        this.csq = csq;
        this.start = start;
        this.end = end;
      }

      @Override
      public Appendable executeAndQuery(Appendable prevalentSystem, Date executionTime) throws Exception {
        return prevalentSystem.append(csq, start, end);
      }

    }

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testVolatilePrevayler() throws Exception {
    StringBuilder builder = new StringBuilder();
    Prevayler<StringBuilder> prevayler = new VolatilePrevayler<>(builder);
    prevayler.execute(new AppendableExecutable.AppendChar('0'));
    prevayler.execute(new AppendableExecutable.AppendCharSequence("123"));
    prevayler.execute(new AppendableExecutable.AppendCharSequenceStartEnd("123456789", 3, 6));
    assertEquals("0123456", builder.toString());
    prevayler.takeSnapshot();
  }

}
