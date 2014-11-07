package org.prevayler.contrib.p8.ported;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.util.CheckpointPrevayler;

public class CheckpointTest extends FileIOBase {

  private Prevayler<AppendingSystem> _prevayler;

  @Test
  public void testCheckpoint() throws Exception {

    crashRecover(); // There is nothing to recover at first. A new system will be created.
    crashRecover();
    append("a", "a");
    append("b", "ab");
    verify("ab");

    crashRecover();
    verify("");

    append("a", "a");
    append("b", "ab");
    snapshot();
    snapshot();
    verify("ab");

    crashRecover();
    snapshot();
    append("c", "abc");
    snapshot();
    append("d", "abcd");
    append("e", "abcde");
    verify("abcde");

    crashRecover();
    append("d", "abcd");
    verify("abcd");

  }

  private void crashRecover() throws Exception {
    if (_prevayler != null)
      _prevayler.close();
    out("CrashRecovery.");
    _prevayler = new CheckpointPrevayler<>(new AppendingSystem(), _testDirectory);
  }

  private void snapshot() throws Exception {
    out("Snapshot.");
    _prevayler.takeSnapshot();
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

  private static void out(Object obj) {
    if (Boolean.getBoolean("out"))
      System.out.println(obj);
  }

}