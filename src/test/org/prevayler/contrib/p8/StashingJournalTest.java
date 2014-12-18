package org.prevayler.contrib.p8;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.prevayler.Transaction;
import org.prevayler.contrib.p8.util.Nio;
import org.prevayler.contrib.p8.util.Stashable;

public class StashingJournalTest {

  @SuppressWarnings("serial")
  public static class Append implements Stashable, Transaction<StringBuilder> {

    private final String string;

    public Append(ByteBuffer source) {
      this.string = Nio.getString(source);
    }

    public Append(String string) {
      Objects.requireNonNull(string);
      this.string = string;
    }

    @Override
    public ByteBuffer stash(ByteBuffer target) {
      return Nio.putString(target, string);
    }

    @Override
    public void executeOn(StringBuilder prevalentSystem, Date executionTime) {
      prevalentSystem.append(string);
    }

  }

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

    try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), folder, true)) {
      assertEquals(0, prevayler.age());
      prevayler.execute(new Append("1"));
      assertEquals(1, prevayler.age());
      prevayler.execute(new Append("2"));
      assertEquals(2, prevayler.age());
      prevayler.execute(new Append("3"));
      assertEquals(3, prevayler.age());
      prevayler.takeSnapshot();
      assertEquals(3, prevayler.age());
      prevayler.execute(new Append("4"));
      assertEquals(4, prevayler.age());
      prevayler.takeSnapshot();
      prevayler.takeSnapshot();
      prevayler.takeSnapshot();
      assertEquals(4, prevayler.age());
      assertEquals("1234", prevayler.prevalentSystem().toString());
      assertEquals(Integer.valueOf(4), prevayler.executeQuery((builder, date) -> builder.length()));
      prevayler.execute(new Append("5"));
      assertEquals(5, prevayler.age());
    }

     try (P8<StringBuilder> prevayler = new P8<>(new StringBuilder(), folder, true)) {
     assertEquals(5, prevayler.age());
     assertEquals("12345", prevayler.prevalentSystem().toString());
     assertEquals(Integer.valueOf(5), prevayler.executeQuery((builder, date) -> builder.length()));
     }
  }

}
