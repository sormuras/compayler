//Prevayler(TM) - The Free-Software Prevalence Layer.
//Copyright (C) 2001-2003 Klaus Wuestefeld
//This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

package org.prevayler.contrib.p8.ported;

import static java.nio.file.Files.createLink;
import static java.nio.file.Files.deleteIfExists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.VolatilePrevayler;

public class CheckpointTest extends FileIOBase {

  public static<P> P reloadFromSnapshot(File snapshotFile, P initialPrevalentSystem) {
    if (!snapshotFile.exists())
      return initialPrevalentSystem;
    
    try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(snapshotFile))) {
      @SuppressWarnings("unused")
      long snapshotTransactionCounter = stream.readLong();
      @SuppressWarnings("unchecked")
      P storedSystem = (P) stream.readObject();
      return storedSystem;
    } catch (Exception e) {
      throw new Error(e);
    }
  }
  
  static class CheckpointPrevayler<P> extends VolatilePrevayler<P> {

    private final File base;
    private final File snapshotFile;

    public CheckpointPrevayler(P prevalentSystem, String baseName) {
      super(reloadFromSnapshot(new File(new File(baseName), "snap.shot"), prevalentSystem));
      this.base = new File(baseName);
      this.snapshotFile = new File(base, "snap.shot");
    }

    @Override
    public File takeSnapshot() throws Exception {
      return takeSnapshot(snapshotFile).toFile();
    }

    public Path takeSnapshot(File snapshotFile) throws Exception {
      File taken = new File(base, "snap-" + Instant.now().toString().replace(':', '-') + ".shot");

      try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(taken))) {
        out.writeLong(0);
        out.writeObject(prevalentSystem());
      }

      Path snapshotLink = snapshotFile.toPath();
      deleteIfExists(snapshotLink);
      createLink(snapshotLink, taken.toPath());

      return snapshotLink;
    }

  }

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
