package org.prevayler.contrib.p8;

import static java.lang.Math.min;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

  private final ByteBuffer buffer;

  public ByteBufferInputStream(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public int read() {
    if (!buffer.hasRemaining())
      return -1;

    return buffer.get();
  }

  public int read(byte[] bytes, int off, int len) {
    len = min(len, buffer.remaining());
    buffer.get(bytes, off, len);
    return len;
  }

}
