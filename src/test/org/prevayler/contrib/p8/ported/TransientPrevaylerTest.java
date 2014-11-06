//Prevayler(TM) - The Free-Software Prevalence Layer.
//Copyright (C) 2001-2003 Klaus Wuestefeld
//This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

package org.prevayler.contrib.p8.ported;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.Transaction;
import org.prevayler.contrib.p8.VolatilePrevayler;

import java.util.Date;

public class TransientPrevaylerTest extends FileIOBase {

  private Prevayler<AppendingSystem> prevayler;

  @Before
  public void setUp() throws Exception {
    prevayler = new VolatilePrevayler<>(new AppendingSystem(), true, null);
  }

  @Test
  public void testTransactionExecution() {
    assertState("");

    append("a");
    assertState("a");

    append("b");
    append("c");
    assertState("abc");
  }

  @Test
  public void testSnapshotAttempt() throws Exception {
    try {
      prevayler.takeSnapshot();
      fail("IOException expected.");
    } catch (UnsupportedOperationException uoe) {
      assertNotNull(uoe.getMessage());
    }
  }

  /**
   * The baptism problem occurs when a Transaction keeps a direct reference to a business object instead of querying for it given the
   * Prevalent System.
   */
  @Test
  public void testFailFastBaptismProblem() {
    append("a");

    AppendingSystem directReference = prevayler.prevalentSystem();
    prevayler.execute(new DirectReferenceTransaction(directReference));

    assertState("a");
  }

  @After
  public void tearDown() throws Exception {
    prevayler = null;
  }

  private void assertState(String expected) {
    String result = prevayler.prevalentSystem().value();
    assertEquals(expected, result);
  }

  private void append(String appendix) {
    prevayler.execute(new Appendix(appendix));
  }

  static private class DirectReferenceTransaction implements Transaction<AppendingSystem> {

    private static final long serialVersionUID = -7885669885494051746L;
    private final AppendingSystem _illegalDirectReference;

    DirectReferenceTransaction(AppendingSystem illegalDirectReference) {
      _illegalDirectReference = illegalDirectReference;
    }

    public void executeOn(AppendingSystem ignored, Date ignoredToo) {
      _illegalDirectReference.append("anything");
    }

  }

}
