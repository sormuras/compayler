package org.prevayler.contrib.p8.ported;

import static org.junit.Assume.assumeFalse;

import java.io.Closeable;
import java.io.File;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.P8;
import org.prevayler.foundation.FileManager;

public class PersistenceTest extends FileIOBase {

  private Prevayler<AppendingSystem> _prevayler;
  private String _prevalenceBase;

  public void tearDown() throws Exception {
    if (_prevayler != null) {
      _prevayler.close();
      _prevayler = null; // allow gc to kick in!
    }
    super.tearDown();
  }

  @Test
  public void testPersistenceWithDiskSync() throws Exception {
    doTestPersistence(true);
  }

  @Test
  public void testPersistenceWithoutDiskSync() throws Exception {
    doTestPersistence(false);
  }

  public void doTestPersistence(boolean journalDiskSync) throws Exception {
    newPrevalenceBase();

    crashRecover(journalDiskSync); // There is nothing to recover at first. A new system will be created.
    crashRecover(journalDiskSync);
    append("a", "a");
    append("b", "ab");
    verify("ab");

    crashRecover(journalDiskSync);
    verify("ab");

    append("c", "abc");
    append("d", "abcd");
    snapshot("snap.shot");
    snapshot("snap.shot");
    verify("abcd");

    crashRecover(journalDiskSync);
    snapshot("snap.shot");
    append("e", "abcde");
    snapshot("snap.shot");
    append("f", "abcdef");
    append("g", "abcdefg");
    verify("abcdefg");

    crashRecover(journalDiskSync);
    append("h", "abcdefgh");
    verify("abcdefgh");

    snapshot("snap.shot");
    _prevayler.close();
    File lastSnapshot = new File(_prevalenceBase, "snap.shot");
    File lastTransactionLog = new File(_prevalenceBase, "slice.journal");
    newPrevalenceBase();
    FileManager.produceDirectory(_prevalenceBase);
    lastSnapshot.renameTo(new File(_prevalenceBase, "snap.shot")); // Moving the file.
    lastTransactionLog.renameTo(new File(_prevalenceBase, "slice.journal"));

    crashRecover(journalDiskSync);
    append("i", "abcdefghi");
    append("j", "abcdefghij");
    crashRecover(journalDiskSync);
    append("k", "abcdefghijk");
    append("l", "abcdefghijkl");
    crashRecover(journalDiskSync);
    append("m", "abcdefghijklm");
    append("n", "abcdefghijklmn");
    crashRecover(journalDiskSync);
    verify("abcdefghijklmn");
  }

  @Test
  public void testDiskSyncPerformance() throws Exception {
    assumeFalse("Ant running.", Boolean.getBoolean("ant.running")); // quit if inside ant/junit execution
    long false1 = doDiskSyncPerformanceRun(false);
    long true1 = doDiskSyncPerformanceRun(true);
    long false2 = doDiskSyncPerformanceRun(false);
    long true2 = doDiskSyncPerformanceRun(true);
    long bestTrue = Math.min(true1, true2);
    long worstFalse = Math.max(false1, false2);
    assertTrue(bestTrue + " should be worse than " + worstFalse + "!", bestTrue > worstFalse);
  }

  private long doDiskSyncPerformanceRun(boolean journalDiskSync) throws Exception {

    newPrevalenceBase();
    crashRecover(journalDiskSync);
    append("a", "a");
    long start = System.nanoTime();
    String expected = "a";

    for (char c = 'b'; c <= 'z'; c++) {
      expected += c;
      append(String.valueOf(c), expected);
    }

    long end = System.nanoTime();
    crashRecover(journalDiskSync);
    verify(expected);

    return end - start;
  }

  public void testNondeterminsticError() throws Exception {
    newPrevalenceBase();
    crashRecover(); // There is nothing to recover at first. A new system will be created.

    append("a", "a");
    append("b", "ab");
    verify("ab");

    NondeterministicErrorTransaction.armBomb(1);
    try {
      _prevayler.execute(new NondeterministicErrorTransaction("c"));
      fail();
    } catch (AssertionFailedError failed) {
      throw failed;
    } catch (Error expected) {
      assertEquals(Error.class, expected.getClass());
      assertEquals("BOOM!", expected.getMessage());
    }

    try {
      _prevayler.execute(new Appendix("x"));
      fail();
    } catch (AssertionFailedError failed) {
      throw failed;
    } catch (Error expected) {
      assertEquals(Error.class, expected.getClass());
      assertEquals("Prevayler is no longer processing transactions due to an Error thrown from an earlier transaction.",
          expected.getMessage());
    }

    try {
      _prevayler.execute(new NullQuery());
      fail();
    } catch (AssertionFailedError failed) {
      throw failed;
    } catch (Error expected) {
      assertEquals(Error.class, expected.getClass());
      assertEquals("Prevayler is no longer processing queries due to an Error thrown from an earlier transaction.", expected.getMessage());
    }

    try {
      _prevayler.prevalentSystem();
      fail();
    } catch (AssertionFailedError failed) {
      throw failed;
    } catch (Error expected) {
      assertEquals(Error.class, expected.getClass());
      assertEquals("Prevayler is no longer allowing access to the prevalent system due to an Error thrown from an earlier transaction.",
          expected.getMessage());
    }

    try {
      _prevayler.takeSnapshot();
      fail();
    } catch (AssertionFailedError failed) {
      throw failed;
    } catch (Error expected) {
      assertEquals(Error.class, expected.getClass());
      assertEquals("Prevayler is no longer allowing snapshots due to an Error thrown from an earlier transaction.", expected.getMessage());
    }

    crashRecover();

    // Note that both the transaction that threw the Error and the
    // subsequent transaction *were* journaled, so they get applied
    // successfully on recovery.
    verify("abcx");
  }

  @Test
  public void testJournalPanic() throws Exception {
    newPrevalenceBase();

    crashRecover();
    append("a", "a");
    append("b", "ab");

    sneakilyCloseUnderlyingJournalStream();

    try {
      _prevayler.execute(new Appendix("x"));
      fail();
    } catch (IllegalStateException aborted) {
      assertNotNull(aborted.getMessage());
      assertNotNull(aborted.getCause());
    }

    try {
      _prevayler.execute(new Appendix("y"));
      fail();
    } catch (IllegalStateException aborted) {
      assertNotNull(aborted.getMessage());
      assertNotNull(aborted.getCause());
    }

    crashRecover();
    verify("ab");
    append("c", "abc");
  }

  private void sneakilyCloseUnderlyingJournalStream() throws Exception {
    ((Closeable) Sneaky.get(_prevayler, "journal.sliceStream")).close();
  }

  private void crashRecover() throws Exception {
    crashRecover(true);
  }

  private void crashRecover(boolean journalDiskSync) throws Exception {
    out("CrashRecovery.");

    if (_prevayler != null) {
      _prevayler.close();
      _prevayler = null;
      System.gc();
    }

    AppendingSystem system = new AppendingSystem();
    File base = new File(prevalenceBase());
    long size = 10 * 1024 * 1024;
    long syncDelay = journalDiskSync ? 0 : Long.MAX_VALUE;
    P8<AppendingSystem> factory = new P8<>(system, base, size, syncDelay);
    _prevayler = factory;
  }

  private File snapshot(String expectedSnapshotFilename) throws Exception {
    out("Snapshot.");
    File snapshotFile = _prevayler.takeSnapshot();
    assertEquals(new File(prevalenceBase(), expectedSnapshotFilename), snapshotFile);
    return snapshotFile;
  }

  private void append(String appendix, String expectedResult) throws Exception {
    out("Appending " + appendix);
    _prevayler.execute(new Appendix(appendix));
    verify(expectedResult);
  }

  private void verify(String expectedResult) {
    out("Expecting result: " + expectedResult);
    assertEquals(expectedResult, system().value());
  }

  private AppendingSystem system() {
    return _prevayler.prevalentSystem();
  }

  private String prevalenceBase() {
    return _prevalenceBase;
  }

  private void newPrevalenceBase() throws Exception {
    _prevalenceBase = _testDirectory + File.separator + System.currentTimeMillis();
  }

  private static void out(Object obj) {
    if (Boolean.getBoolean("out"))
      System.out.println(obj);
  }

}
