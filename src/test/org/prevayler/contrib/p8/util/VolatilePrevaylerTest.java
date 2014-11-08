package org.prevayler.contrib.p8.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.prevayler.Transaction;

public class VolatilePrevaylerTest {

  @Test(expected = UnsupportedOperationException.class)
  public void testVolatilePrevayler() throws Exception {
    StringBuilder builder = new StringBuilder();
    try (VolatilePrevayler<StringBuilder> prevayler = new VolatilePrevayler<>(builder)) {
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append('0'));
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append("123"));
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append("123456789", 3, 6));
      assertEquals("0123456", builder.toString());
      prevayler.takeSnapshot();
    }
  }

}
