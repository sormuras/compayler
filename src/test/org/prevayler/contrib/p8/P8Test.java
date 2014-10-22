package org.prevayler.contrib.p8;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.Transaction;
import org.prevayler.contrib.compayler.TestTool;

public class P8Test {

  @Rule
  public final TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testAgeIsSnapshotInvariant() throws Exception {
    File folder = temp.newFolder();

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

  private <P> void testNaivMicroBenchmark(Prevayler<StringBuilder> prevayler) {
    for (int i = 0; i < 1000 * 1000; i++) {
      prevayler.execute((Transaction<StringBuilder>) (builder, date) -> builder.append("1"));
    }
  }

  @Test
  public void testNaivMicroBenchmarkWithDefaultPrevayler() throws Exception {
    Prevayler<StringBuilder> prevayler = TestTool.prevayler(new StringBuilder(), temp.newFolder());
    try {
      testNaivMicroBenchmark(prevayler);
    } finally {
      prevayler.close();
    }
  }

  @Test
  public void testNaivMicroBenchmarkWithP8() throws Exception {
    try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), temp.newFolder())) {
      testNaivMicroBenchmark(prevayler);
    }
  }

}
