package org.prevayler.contrib.p8.ported;

import java.util.Date;
import java.util.LinkedList;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.TransactionWithQuery;
import org.prevayler.contrib.p8.util.VolatilePrevayler;

public class QueryExecutionTest extends FileIOBase {

  @Test
  public void testQuery() throws Exception {
    LinkedList<?> prevalentSystem = new LinkedList<>();
    Prevayler<LinkedList<?>> prevayler = new VolatilePrevayler<>(prevalentSystem);
    Integer result = prevayler.execute(query());
    assertEquals(0, result.intValue());
  }

  @SuppressWarnings("serial")
  private static Query<LinkedList<?>, Integer> query() {
    return new Query<LinkedList<?>, Integer>() {
      public Integer query(LinkedList<?> prevalentSystem, Date ignored) throws Exception {
        return new Integer(prevalentSystem.size());
      }
    };
  }

  @Test
  public void testTransactionWithQuery() throws Exception {
    LinkedList<String> prevalentSystem = new LinkedList<>();
    Prevayler<LinkedList<String>> prevayler = new VolatilePrevayler<>(prevalentSystem);
    String result = prevayler.execute(transactionWithQuery());
    assertEquals("abc", result);
    assertEquals("added element", prevalentSystem.get(0));
  }

  private static TransactionWithQuery<LinkedList<String>, String> transactionWithQuery() {
    return new TransactionWithQuery<LinkedList<String>, String>() {
      private static final long serialVersionUID = -2976662596936807721L;

      public String executeAndQuery(LinkedList<String> prevalentSystem, Date timestamp) {
        prevalentSystem.add("added element");
        return "abc";
      }
    };
  }

}
