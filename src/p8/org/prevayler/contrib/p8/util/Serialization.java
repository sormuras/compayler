package org.prevayler.contrib.p8.util;

import static java.nio.file.Files.createLink;
import static java.nio.file.Files.deleteIfExists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.time.Instant;

import org.prevayler.foundation.ObjectInputStreamWithClassLoader;

public class Serialization {

  @FunctionalInterface
  public interface HeaderReader {
    void read(ObjectInputStream in) throws IOException;
  }
  
  @FunctionalInterface
  public interface HeaderWriter {
    void write(ObjectOutputStream out) throws IOException;
  }

  public static byte[] toBytes(Object object, ByteArrayOutputStream byteArrayOutputStream) {
    byteArrayOutputStream.reset();
    try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
      out.writeObject(object);
      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Serialization failed!", e);
    }
  }

  public static Object toObject(byte[] bytes, ClassLoader classLoader) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStreamWithClassLoader(byteArrayInputStream, classLoader)) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Deserialization failed!", e);
    }
  }

  public static <P> P toPrevalentSystem(String baseName, P initialPrevalentSystem, ClassLoader classLoader) {
    return toPrevalentSystem(new File(new File(baseName), "snap.shot"), initialPrevalentSystem, classLoader, null);
  }

  public static <P> P toPrevalentSystem(File snapshotFile, P initialPrevalentSystem, ClassLoader classLoader, HeaderReader reader) {
    if (!snapshotFile.exists())
      return initialPrevalentSystem;

    try (ObjectInputStream stream = new ObjectInputStreamWithClassLoader(new FileInputStream(snapshotFile), classLoader)) {
      if (reader != null)
        reader.read(stream);
      @SuppressWarnings("unchecked")
      P storedSystem = (P) stream.readObject();
      return storedSystem;
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  public static <P> Path toSnapshot(P prevalentSystem, File base) throws Exception {
    return toSnapshot(prevalentSystem, base, new File(base, "snap.shot"), null);
  }

  public static <P> Path toSnapshot(P prevalentSystem, File base, File snapshotFile, HeaderWriter writer) throws Exception {
    File taken = new File(base, "snap-" + Instant.now().toString().replace(':', '-') + ".shot");

    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(taken))) {
      if (writer != null)
        writer.write(out);
      out.writeObject(prevalentSystem);
    }

    if (snapshotFile == null)
      return taken.toPath();

    Path snapshotLink = snapshotFile.toPath();
    deleteIfExists(snapshotLink);
    createLink(snapshotLink, taken.toPath());

    return snapshotLink;
  }

}