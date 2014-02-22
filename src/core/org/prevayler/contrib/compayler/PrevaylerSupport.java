package org.prevayler.contrib.compayler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.foundation.ObjectInputStreamWithClassLoader;
import org.prevayler.foundation.monitor.Monitor;
import org.prevayler.foundation.monitor.SimpleMonitor;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.PrevaylerDirectory;
import org.prevayler.implementation.PrevaylerImpl;
import org.prevayler.implementation.clock.MachineClock;
import org.prevayler.implementation.journal.Journal;
import org.prevayler.implementation.journal.PersistentJournal;
import org.prevayler.implementation.publishing.CentralPublisher;
import org.prevayler.implementation.publishing.TransactionPublisher;
import org.prevayler.implementation.snapshot.GenericSnapshotManager;

public class PrevaylerSupport {

  public static class DefaultPrevaylerFactory<P> implements PrevaylerFactory<P> {

    private final P prevalentSystem;

    public DefaultPrevaylerFactory(P prevalentSystem) {
      this.prevalentSystem = prevalentSystem;
    }

    @Override
    public Prevayler<P> createPrevayler(ClassLoader loader) throws Exception {
      return PrevaylerSupport.createPrevayler(prevalentSystem, loader);
    }

  }

  public static class VolatilePrevayler<P> implements Prevayler<P> {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ClassLoader classLoader;
    private final Clock clock;
    private final P prevalentSystem;

    public VolatilePrevayler(P prevalentSystem) {
      this(prevalentSystem, Thread.currentThread().getContextClassLoader());
    }

    public VolatilePrevayler(P prevalentSystem, ClassLoader classLoader) {
      this.prevalentSystem = prevalentSystem;
      this.classLoader = classLoader;
      this.clock = new MachineClock();
      this.byteArrayOutputStream = new ByteArrayOutputStream(0xFFFF);
    }

    @Override
    public Clock clock() {
      return clock;
    }

    @Override
    public void close() throws IOException {
      byteArrayOutputStream.close();
    }

    @Override
    public <R> R execute(Query<? super P, R> sensitiveQuery) throws Exception {
      return sensitiveQuery.query(prevalentSystem, clock().time()); // no copy needed - queries are transient by contract
    }

    @Override
    public <R> R execute(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
      return testCopy(sureTransactionWithQuery).executeAndQuery(prevalentSystem, clock().time());
    }

    @Override
    public void execute(Transaction<? super P> transaction) {
      testCopy(transaction).executeOn(prevalentSystem, clock().time());
    }

    @Override
    public <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
      return testCopy(transactionWithQuery).executeAndQuery(prevalentSystem, clock().time());
    }

    @Override
    public P prevalentSystem() {
      return prevalentSystem;
    }

    @Override
    public File takeSnapshot() throws Exception {
      throw new UnsupportedOperationException();
    }

    protected <T> T testCopy(T object) {
      byte[] bytes = toBytes(object);
      @SuppressWarnings("unchecked")
      T result = (T) toObject(bytes);
      byte[] results = toBytes(result);
      if (Arrays.equals(bytes, results)) {
        return result;
      }
      throw new IllegalStateException("Object binary form mismatch. Serialization is broken?! " + object);
    }

    protected byte[] toBytes(Object object) {
      byteArrayOutputStream.reset();
      try (ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream)) {
        out.writeObject(object);
        return byteArrayOutputStream.toByteArray();
      } catch (Exception e) {
        throw new RuntimeException("Serialization failed!", e);
      }
    }

    protected Object toObject(byte[] bytes) {
      try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      // or new org.apache.commons.io.input.ClassLoaderObjectInputStream(classLoader, byteArrayInputStream)
          ObjectInputStream in = new ObjectInputStreamWithClassLoader(byteArrayInputStream, classLoader)) {
        return in.readObject();
      } catch (Exception e) {
        throw new RuntimeException("Deserialization failed!", e);
      }
    }

  }

  public static class VolatilePrevaylerFactory<P> implements PrevaylerFactory<P> {

    private final P prevalentSystem;

    public VolatilePrevaylerFactory(P prevalentSystem) {
      this.prevalentSystem = prevalentSystem;
    }

    @Override
    public Prevayler<P> createPrevayler(ClassLoader loader) {
      return new VolatilePrevayler<>(prevalentSystem, loader);
    }

  }

  public static <P> Prevayler<P> createPrevayler(P prevalentSystem, ClassLoader loader) throws Exception {
    return createPrevayler(prevalentSystem, loader, "PrevalenceBase");
  }

  public static <P> Prevayler<P> createPrevayler(P prevalentSystem, ClassLoader loader, String folder) throws Exception {
    PrevaylerDirectory directory = new PrevaylerDirectory(folder);
    Monitor monitor = new SimpleMonitor(System.err);
    Journal journal = new PersistentJournal(directory, 0, 0, true, "journal", monitor);
    Serializer serializer = new JavaSerializer(loader);
    Map<String, Serializer> map = Collections.singletonMap("snapshot", serializer);
    GenericSnapshotManager<P> snapshotManager = new GenericSnapshotManager<>(map, "snapshot", prevalentSystem, directory, serializer);
    Clock clock = new MachineClock();
    TransactionPublisher publisher = new CentralPublisher(clock, journal);
    return new PrevaylerImpl<>(snapshotManager, publisher, serializer, true);
  }

}
