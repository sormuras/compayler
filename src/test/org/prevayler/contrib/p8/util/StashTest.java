package org.prevayler.contrib.p8.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import org.prevayler.contrib.p8.util.Stash.Realm;

public class StashTest implements Realm {

  private int handleCounter;

  @Test
  public void testStash() throws Exception {
    assertEquals(0, handleCounter);
    int max = 1000;

    ByteBuffer buffer = ByteBuffer.allocateDirect(1400 + 8 + 42 * max);
    Stash stash = new Stash(buffer, this);
    assertNotNull(stash);
    assertEquals(8, buffer.position());
    assertEquals(1d, stash.usage(), 1d);

    for (int time = 0; time < max; time++) {
      stash.stash(new StashableTest.Alpha(time), time, true);
    }
    assertEquals(100d, stash.usage(), 1d);
    stash.close();

    int last = buffer.position();
    buffer.rewind();

    Stash treasure = new Stash(buffer, this);
    assertNotNull(treasure);
    assertEquals(last, buffer.position());
    assertEquals(max, handleCounter);
  }

  @Test
  public void testStashWithThreeStashables() throws Exception {
    assertEquals(0, handleCounter);

    ByteBuffer buffer = ByteBuffer.allocateDirect(10 * 1024 * 1024);
    try (Stash stash = new Stash(buffer, this)) {
      stash.stash(new StashableTest.Alpha(456), System.currentTimeMillis(), true);
      stash.stash(new StashableTest.Beta(7347), System.currentTimeMillis(), true);
      stash.stash(new StashableTest.Data(3874, null, "s", "t"), System.currentTimeMillis(), true);
    }

    buffer.rewind();

    try (Stash stash = new Stash(buffer, this)) {
      assertEquals(3, handleCounter);
    }
  }

  @Override
  public void handle(long current, long total, long time, Stashable stashable) {
    // System.out.println(time + "| " + stashable);
    handleCounter++;
  }

  @Override
  public void close() throws IOException {
    // nothing to close
  }

}
