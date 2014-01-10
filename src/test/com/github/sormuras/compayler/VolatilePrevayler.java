package com.github.sormuras.compayler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.foundation.ObjectInputStreamWithClassLoader;
import org.prevayler.implementation.clock.MachineClock;

public class VolatilePrevayler<P> implements Prevayler<P> {

  private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0xFFFF);
  private final ClassLoader classLoader;
  private final Clock clock;
  private final P prevalentSystem;

  public VolatilePrevayler(P prevalentSystem) {
    this(prevalentSystem, Thread.currentThread().getContextClassLoader());
  }

  public VolatilePrevayler(P prevalentSystem, ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.clock = new MachineClock();
    this.prevalentSystem = prevalentSystem;
  }

  @SuppressWarnings("unchecked")
  protected <T> T copy(T object) {
    byte[] bytes = toBytes(object);
    Object result = toObject(bytes);
    byte[] results = toBytes(result);
    if (Arrays.equals(bytes, results)) {
      return (T) result;
    }
    throw new IllegalStateException("Object binary form mismatch. Serialization is broken?! " + object);
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
    return copy(sureTransactionWithQuery).executeAndQuery(prevalentSystem, clock().time());
  }

  @Override
  public void execute(Transaction<? super P> transaction) {
    copy(transaction).executeOn(prevalentSystem, clock().time());
  }

  @Override
  public <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    return copy(transactionWithQuery).executeAndQuery(prevalentSystem, clock().time());
  }

  @Override
  public P prevalentSystem() {
    return prevalentSystem;
  }

  @Override
  public File takeSnapshot() throws Exception {
    throw new UnsupportedOperationException();
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
