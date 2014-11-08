package org.prevayler.contrib.p8.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.prevayler.Transaction;

public class CheckpointPrevaylerTest {

  @Rule
  public final TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public final TestName testName = new TestName();

  @Test
  public void testCheckpointPrevayler() throws Exception {
    File folder = temp.newFolder(testName.getMethodName());

    // empty on first start
    try (CheckpointPrevayler<StringBuilder> prevayler = new CheckpointPrevayler<>(new StringBuilder(), folder.getAbsolutePath())) {
      assertEquals("", prevayler.prevalentSystem().toString());
    }

    // still empty, no snapshot taken -> append "0123" and take snapshot
    try (CheckpointPrevayler<StringBuilder> prevayler = new CheckpointPrevayler<>(new StringBuilder(), folder.getAbsolutePath())) {
      assertEquals("", prevayler.prevalentSystem().toString());
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append('0'));
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append("123"));
      assertEquals("0123", prevayler.prevalentSystem().toString());
      prevayler.takeSnapshot();
      assertEquals("0123", prevayler.prevalentSystem().toString());
    }

    // got "0123" from previous run -> append "456" w/o taking new snapshot
    try (CheckpointPrevayler<StringBuilder> prevayler = new CheckpointPrevayler<>(new StringBuilder(), folder.getAbsolutePath())) {
      assertEquals("0123", prevayler.prevalentSystem().toString());
      prevayler.execute((Transaction<StringBuilder>) (p, d) -> p.append("123456789", 3, 6));
      assertEquals("0123456", prevayler.prevalentSystem().toString());
    }

    // must still be "0123" here
    try (CheckpointPrevayler<StringBuilder> prevayler = new CheckpointPrevayler<>(new StringBuilder(), folder.getAbsolutePath())) {
      assertEquals("0123", prevayler.prevalentSystem().toString());
    }

  }

}
