package org.prevayler.contrib.p8;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

public class Journal<P> implements Closeable, Flushable {

  /**
   * Force flush every 11 seconds...
   */
  public static final long DEFAULT_NANOS_BETWEEN_FORCE_FLUSH = TimeUnit.NANOSECONDS.convert(11L, TimeUnit.SECONDS);

  /**
   * Nano timestamp of last forced flush.
   */
  private long forcedFlushNanoTime = System.nanoTime();

  /**
   * Direct byte buffer whose content is the memory-mapped journal file.
   */
  private final MappedByteBuffer memory;

  /**
   * Random access journal file.
   */
  private final RandomAccessFile memoryFile;

  /**
   * Amount of all slices in the journal file.
   */
  private int memorySliceCounter;

  /**
   * Amount of all transaction in the journal file. That is the sum of all transactions of all slices.
   */
  private long memoryTransactionCounter;

  /**
   * Associated prevayler instance.
   */
  private final P8<P> prevayler;

  /**
   * Sliced view on the journal buffer.
   */
  private ByteBuffer slice;

  /**
   * Object output stream writing to current slices' buffer.
   */
  private ObjectOutputStream sliceStream;

  /**
   * Amount of all transactions in current slice.
   */
  private int sliceTransactionCounter;

  public Journal(P8<P> prevayler, File journalFile, long size) throws Exception {
    this.prevayler = prevayler;

    this.memoryFile = new RandomAccessFile(journalFile, "rw");
    if (memoryFile.length() == 0) {
      memoryFile.setLength(size);
    }

    this.memory = memoryFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);

    replay();
    slice();
  }

  private void replay() throws Exception {
    this.memorySliceCounter = memory.getInt();
    this.memoryTransactionCounter = memory.getLong();

    for (int sliceNumber = 0; sliceNumber < memorySliceCounter; sliceNumber++) {
      long transactions = memory.getInt();
      try (ObjectInputStream in = new ObjectInputStream(new ByteBufferInputStream(memory))) {
        for (int j = 0; j < transactions; j++) {
          long time = in.readLong();
          Object object = in.readObject();
          prevayler.executeVolatile(object, time);
        }
      }
    }
  }

  public void clear() throws IOException {
    memory.clear(); // TODO Erase buffer?
    memory.putInt(memorySliceCounter = 0);
    memory.putLong(memoryTransactionCounter = 0);

    slice();
  }

  @Override
  public void close() throws IOException {
    sliceStream.close();
    memory.force();
    memoryFile.close();
  }

  public void commit() {
    slice.putInt(0, ++sliceTransactionCounter);
    memory.putLong(4, ++memoryTransactionCounter);

    if (System.nanoTime() - forcedFlushNanoTime >= DEFAULT_NANOS_BETWEEN_FORCE_FLUSH)
      flush();
  }

  public <T> T copy(T object, long time) {
    try {
      sliceStream.writeLong(time);
      sliceStream.writeObject(object);
      // @SuppressWarnings("unchecked")
      // T copy = P8.class.desiredAssertionStatus() ? (T) DeepCopier.deepCopy(object, new JavaSerializer()) : object;
      // return copy;
      return object;
    } catch (BufferOverflowException e) {
      throw new Error("Increase initial journal file size!", e);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private void slice() throws IOException {
    slice = memory.slice();
    slice.putInt(sliceTransactionCounter = 0);

    sliceStream = new ObjectOutputStream(new ByteBufferOutputStream(slice));
    sliceStream.flush();

    memory.putInt(0, memorySliceCounter + 1);

    flush();
  }

  public double usage() {
    return 100d - slice.remaining() * 100d / memory.capacity();
  }

  @Override
  public void flush() {
    memory.force();
    forcedFlushNanoTime = System.nanoTime();
  }

  public long getMemoryTransactionCounter() {
    return memoryTransactionCounter;
  }

}