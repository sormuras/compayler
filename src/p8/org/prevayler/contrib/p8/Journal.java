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

import org.prevayler.contrib.p8.util.ByteBufferInputStream;
import org.prevayler.contrib.p8.util.ByteBufferOutputStream;

public class Journal<P> implements Closeable, Flushable {
  
  /**
   * Amount of all transaction in the journal file. That is the sum of all transactions of all slices.
   */
  private long age;

  /**
   * Nano timestamp of last flush.
   */
  private long forcedFlushNanoTime = System.nanoTime();

  /**
   * Nanos between forced flushes.
   */
  private long forceNextFlushAfterNanos = Long.MAX_VALUE;

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

  public Journal(P8<P> prevayler, File journalFile, long size, long nanos) throws Exception {
    this.prevayler = prevayler;

    this.memoryFile = new RandomAccessFile(journalFile, "rw");
    if (memoryFile.length() == 0) {
      memoryFile.setLength(size);
    }

    this.memory = memoryFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, size);
    this.forceNextFlushAfterNanos = nanos;

    build();
    slice();
  }

  protected void build() throws Exception {
    this.memorySliceCounter = memory.getInt();
    this.age = memory.getLong();

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
    memory.putLong(age = 0);

    slice();
  }

  @Override
  public void close() throws IOException {
    try {
      sliceStream.close();
    } catch (IOException e) {
      // ignore
    }
    memory.force();
    memoryFile.close();
  }

  public void commit() {
    slice.putInt(0, ++sliceTransactionCounter);
    memory.putLong(4, ++age);

    if (System.nanoTime() - forcedFlushNanoTime >= getForceNextFlushAfterNanos())
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
    } catch (IOException e) {
      throw new IllegalStateException("Could not copy transaction of " + object.getClass().getSimpleName(), e);
    }
  }

  @Override
  public void flush() {
    memory.force();
    forcedFlushNanoTime = System.nanoTime();
  }

  public long getAge() {
    return age;
  }

  public long getForceNextFlushAfterNanos() {
    return forceNextFlushAfterNanos;
  }

  public void setForceNextFlushAfterNanos(long forceNextFlushAfterNanos) {
    this.forceNextFlushAfterNanos = forceNextFlushAfterNanos;
  }

  protected void slice() throws IOException {
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

}