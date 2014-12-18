package org.prevayler.contrib.p8;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.prevayler.contrib.p8.util.Stash;
import org.prevayler.contrib.p8.util.Stashable;

public class StashingJournal extends Stash implements J8 {

  public static class MemoryHolder implements Realm {

    /**
     * Direct byte buffer whose content is the memory-mapped journal file.
     */
    private final MappedByteBuffer memory;

    /**
     * Random access journal file.
     */
    private final RandomAccessFile memoryFile;

    private final P8<?> p8;

    public MemoryHolder(P8<?> p8, File journalFile, long size) throws IOException {
      this.p8 = p8;

      this.memoryFile = new RandomAccessFile(journalFile, "rw");
      if (memoryFile.length() == 0) {
        memoryFile.setLength(size);
      }

      this.memory = memoryFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
    }

    @Override
    public void close() throws IOException {
      memory.force();
      memoryFile.close();
    }

    @Override
    public void handle(long current, long total, long time, Stashable stashable) {
      try {
        p8.executeVolatile(stashable, time);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public ByteBuffer getByteBuffer() {
      return memory;
    }

  }

  private final MemoryHolder holder;

  public StashingJournal(MemoryHolder holder) {
    super(holder.getByteBuffer(), holder);

    this.holder = holder;
  }

  @Override
  public void flush() throws IOException {
    holder.memory.force();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T copy(T object, long time) {
    return (T) super.stash((Stashable) object, time, false);
  }

}
