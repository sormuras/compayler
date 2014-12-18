package org.prevayler.contrib.p8.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteBufferOutputStream extends OutputStream {

  private final AtomicBoolean closed;
  private final ByteBuffer buffer;

  public ByteBufferOutputStream(ByteBuffer buffer) {
    this.buffer = buffer;
    this.closed = new AtomicBoolean(false);
  }

  @Override
  public void close() {
    closed.set(true);
  }

  public void write(byte[] bytes, int off, int len) throws IOException {
    if (closed.get())
      throw new IOException("Can not write " + len + " bytes to closed stream!");
    buffer.put(bytes, off, len);
  }

  public void write(int b) throws IOException {
    if (closed.get())
      throw new IOException("Can not write byte to closed stream!");
    buffer.put((byte) b);
  }

}
