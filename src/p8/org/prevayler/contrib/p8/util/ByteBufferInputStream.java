package org.prevayler.contrib.p8.util;

import static java.lang.Math.min;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ByteBufferInputStream extends InputStream {

  private final ByteBuffer buffer;
  private final AtomicBoolean closed;

  public ByteBufferInputStream(ByteBuffer buffer) {
    this.buffer = buffer;
    this.closed = new AtomicBoolean(false);
  }

  @Override
  public void close() {
    closed.set(true);
  }

  public int read() throws IOException {
    if (closed.get())
      throw new IOException("Can not read bytes from closed input stream!");

    if (!buffer.hasRemaining())
      return -1;

    return buffer.get();
  }

  public int read(byte[] bytes, int off, int len) throws IOException {
    if (closed.get())
      throw new IOException("Can not read bytes from closed input stream!");

    if (!buffer.hasRemaining())
      return -1;

    len = min(len, buffer.remaining());
    buffer.get(bytes, off, len);
    return len;
  }

}
