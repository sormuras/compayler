package org.prevayler.contrib.p8.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Nio {

  static boolean getBoolean(ByteBuffer source) {
    return source.get() == 1;
  }

  static <E extends Enum<E>> E getEnum(Class<E> enumClass, ByteBuffer source) {
    return enumClass.getEnumConstants()[getInt7(source)];
  }

  static int getInt7(ByteBuffer source) {
    int b = source.get();
    if (b >= 0)
      return b;

    int result = b & 0x7F;
    int shift = 1;
    while (true) {
      b = source.get();
      result |= (b & 0x7F) << (7 * shift);
      if (b >= 0)
        return result;

      shift++;
    }
  }

  static long getLong7(ByteBuffer source) {
    long b = source.get();
    if (b >= 0)
      return b;

    long result = b & 0x7F;
    int shift = 1;
    while (true) {
      b = source.get();
      result |= (b & 0x7F) << (7 * shift);
      if (b >= 0)
        return result;

      shift++;
    }
  }

  static String getString(ByteBuffer source) {
    return getString(source, StandardCharsets.UTF_8);
  }

  static String getString(ByteBuffer source, Charset charset) {
    byte[] bytes = new byte[source.getShort()];
    source.get(bytes);
    return new String(bytes, charset);
  }

  static ByteBuffer putBoolean(ByteBuffer target, boolean value) {
    return target.put((byte) (value ? 1 : 0));
  }

  static <E extends Enum<E>> ByteBuffer putEnum(ByteBuffer target, E value) {
    return putInt7(target, value.ordinal());
  }

  static ByteBuffer putInt7(ByteBuffer target, int value) {
    assert value >= 0 : "value must be positive: " + value;
    while (value > Byte.MAX_VALUE) {
      target.put((byte) (0x80 | (value & 0x7F)));
      value >>>= 7;
    }
    return target.put((byte) value);
  }

  static ByteBuffer putLong7(ByteBuffer target, long value) {
    assert value >= 0 : "value must be positive: " + value;
    while (value > Byte.MAX_VALUE) {
      target.put((byte) (0x80 | (value & 0x7F)));
      value >>>= 7;
    }
    return target.put((byte) value);
  }

  static ByteBuffer putString(ByteBuffer target, String string) {
    return putString(target, string, StandardCharsets.UTF_8);
  }

  static ByteBuffer putString(ByteBuffer target, String string, Charset charset) {
    byte[] bytes = string.getBytes(charset);
    target.putShort((short) bytes.length);
    target.put(bytes);
    return target;
  }

  /**
   * Counts all remaining bytes.
   * 
   * @param buffers
   *          to scan
   * @return amount of remaining bytes
   */
  static long remaining(ByteBuffer[] buffers) {
    assert buffers != null;
    long remaining = 0;
    for (int i = 0; i < buffers.length; i++) {
      remaining += buffers[i].remaining();
    }
    return remaining;
  }

  /**
   * Skip over and discards {@code n} bytes of data from the passed byte buffer.
   * <p>
   * The <code>skip</code> method may, for a variety of reasons, end up skipping over a smaller number of bytes (possibly {@code 0}).
   * Reaching buffers' limit before {@code n} bytes are skipped is only one possibility. The actual number of bytes skipped is returned. If
   * {@code n} is negative, no bytes are skipped.
   * 
   * @param buffer
   *          The buffer to skip {@code n} bytes.
   * @param n
   *          The number of bytes to skip.
   */
  static int skip(ByteBuffer buffer, int n) {
    if (n <= 0) {
      return 0;
    }
    int newPosition = buffer.position() + n;
    if (newPosition > buffer.limit()) {
      newPosition = buffer.limit();
      n = buffer.remaining();
    }
    buffer.position(newPosition);
    return n;
  }

  /**
   * Same as: <code>toString(bytes, 16, 16)</code>.
   */
  static String toString(byte[] bytes) {
    return toString(bytes, 16, 16);
  }

  /**
   * Same as: <code>toString(ByteBuffer.wrap(bytes), bytesPerLine, maxLines)</code>.
   */
  static String toString(byte[] bytes, int bytesPerLine, int maxLines) {
    return toString(ByteBuffer.wrap(bytes), bytesPerLine, maxLines);
  }

  /**
   * Same as: <code>toString(buffer, 16, 16)</code>.
   */
  static String toString(ByteBuffer buffer) {
    return toString(buffer, 16, 16);
  }

  static String toString(ByteBuffer buffer, int bytesPerLine, int maxLines) {
    return toString(new StringBuilder(), buffer, bytesPerLine, maxLines);
  }

  static String toString(StringBuilder builder, ByteBuffer buffer) {
    return toString(builder, buffer, 16, 16);
  }

  static String toString(StringBuilder builder, ByteBuffer buffer, int bytesPerLine, int maxLines) {
    final boolean INCLUDE_SEGMENT_NUMBERS = true;
    final boolean INCLUDE_VIEW_HEX = true;
    final boolean INCLUDE_VIEW_ASCII = true;
    final int BLOCK_LENGTH = 4;
    final char BLOCK_SEPARATOR = ' ';
    int i, j, n, k, line;
    builder.append(buffer).append(" {\n");
    line = 0;
    for (n = 0; n < buffer.remaining(); n += bytesPerLine, line++) {
      // builder.append(" ");
      if (line >= maxLines) {
        int omitted = buffer.remaining() - n;
        builder.append("...(");
        builder.append(omitted);
        builder.append(" byte");
        builder.append(omitted != 1 ? "s" : "");
        builder.append(" omitted)\n");
        break;
      }
      if (INCLUDE_SEGMENT_NUMBERS) {
        String segment = Integer.toHexString(n).toUpperCase();
        for (j = 0, k = 4 - segment.length(); j < k; j++) {
          builder.append('0');
        }
        builder.append(segment).append(" | ");
      }
      if (INCLUDE_VIEW_HEX) {
        for (i = n; i < n + bytesPerLine && i < buffer.remaining(); i++) {
          String s = Integer.toHexString(buffer.get(i) & 255).toUpperCase();
          if (s.length() == 1) {
            builder.append('0');
          }
          builder.append(s).append(' ');
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
        }
        while (i < n + bytesPerLine) {
          builder.append("   ");
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
          i++;
        }
        builder.append('|').append(' ');
      }
      if (INCLUDE_VIEW_ASCII) {
        for (i = n; i < n + bytesPerLine && i < buffer.remaining(); i++) {
          int v = buffer.get(i) & 255;
          if (v > 127 || Character.isISOControl((char) v)) {
            builder.append('.');
          } else {
            builder.append((char) v);
          }
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
        }
        while (i < n + bytesPerLine) {
          builder.append(' ');
          if (i % bytesPerLine % BLOCK_LENGTH == BLOCK_LENGTH - 1 && i < n + bytesPerLine - 1) {
            builder.append(BLOCK_SEPARATOR);
          }
          i++;
        }
      }
      builder.append('\n');
    }
    builder.append("}");
    return builder.toString();
  }

}
