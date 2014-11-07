package org.prevayler.contrib.p8;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

public class P8Test {

  @Rule
  public final TemporaryFolder temp = new TemporaryFolder();

  @Rule
  public final TestName testName = new TestName();

  @After
  public void after() {
    System.gc(); // release mmap handle
  }

  @Test
  public void testAgeIsSnapshotInvariant() throws Exception {
    File folder = temp.newFolder(testName.getMethodName());

    try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), folder)) {
      assertEquals(0, prevayler.age());
      prevayler.executeTransaction((builder, date) -> builder.append("1"));
      assertEquals(1, prevayler.age());
      prevayler.executeTransaction((builder, date) -> builder.append("2"));
      assertEquals(2, prevayler.age());
      prevayler.executeTransaction((builder, date) -> builder.append("3"));
      assertEquals(3, prevayler.age());
      prevayler.takeSnapshot();
      assertEquals(3, prevayler.age());
      prevayler.executeTransaction((builder, date) -> builder.append("4"));
      assertEquals(4, prevayler.age());
      prevayler.takeSnapshot();
      prevayler.takeSnapshot();
      prevayler.takeSnapshot();
      assertEquals(4, prevayler.age());
      assertEquals("1234", prevayler.prevalentSystem().toString());
      assertEquals(Integer.valueOf(4), prevayler.executeQuery((builder, date) -> builder.length()));
      prevayler.executeTransaction((builder, date) -> builder.append("5"));
      assertEquals(5, prevayler.age());
    }

    try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), folder)) {
      assertEquals(5, prevayler.age());
      assertEquals("12345", prevayler.prevalentSystem().toString());
      assertEquals(Integer.valueOf(5), prevayler.executeQuery((builder, date) -> builder.length()));
    }
  }

}
