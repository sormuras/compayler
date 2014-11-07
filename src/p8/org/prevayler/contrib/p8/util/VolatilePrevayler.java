package org.prevayler.contrib.p8.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.implementation.clock.MachineClock;

public class VolatilePrevayler<P> implements Prevayler<P> {

  private final ByteArrayOutputStream byteArrayOutputStream;
  private final ClassLoader classLoader;
  private final Clock clock;
  private final boolean deepCopy;
  private final P prevalentSystem;

  public VolatilePrevayler(P prevalentSystem) {
    this(prevalentSystem, false, null);
  }

  public VolatilePrevayler(P prevalentSystem, boolean deepCopy, ClassLoader classLoader) {
    this.prevalentSystem = prevalentSystem;
    this.deepCopy = deepCopy;
    this.classLoader = classLoader;
    this.clock = new MachineClock();
    this.byteArrayOutputStream = new ByteArrayOutputStream(deepCopy ? 0xFFFF : 0);
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
    throw new UnsupportedOperationException("This is a volatile prevayler. No snap, no shot.");
  }

  protected <T> T copy(T object) {
    if (!deepCopy)
      return object;
    byte[] bytes = Serialization.toBytes(object, byteArrayOutputStream);
    @SuppressWarnings("unchecked")
    T result = (T) Serialization.toObject(bytes, classLoader);
    byte[] results = Serialization.toBytes(object, byteArrayOutputStream);
    if (Arrays.equals(bytes, results)) {
      return result;
    }
    throw new IllegalStateException("Object binary form mismatch. Serialization is broken?! " + object);
  }

  @Override
  public String toString() {
    return "VolatilePrevayler";
  }

}