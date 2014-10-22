package org.prevayler.contrib.p8;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream {

  private final ByteBuffer buffer;

  public ByteBufferOutputStream(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public void write(int b) {
    buffer.put((byte) b);
  }

  public void write(byte[] bytes, int off, int len) {
    buffer.put(bytes, off, len);
  }

}
