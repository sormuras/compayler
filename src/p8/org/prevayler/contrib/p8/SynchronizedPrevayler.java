package org.prevayler.contrib.p8;

import java.io.File;
import java.io.IOException;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

public class SynchronizedPrevayler<P> implements Prevayler<P>, AutoCloseable {

  private final Prevayler<P> prevayler;

  public SynchronizedPrevayler(Prevayler<P> prevayler) {
    this.prevayler = prevayler;
  }

  @Override
  public Clock clock() {
    return prevayler.clock();
  }

  @Override
  public synchronized void close() throws IOException {
    prevayler.close();
  }

  @Override
  public synchronized void execute(Transaction<? super P> transaction) {
    prevayler.execute(transaction);
  }

  @Override
  public synchronized <R> R execute(Query<? super P, R> query) throws Exception {
    return prevayler.execute(query);
  }

  @Override
  public synchronized <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    return prevayler.execute(transactionWithQuery);
  }

  @Override
  public synchronized <R> R execute(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
    return prevayler.execute(sureTransactionWithQuery);
  }

  @Override
  public P prevalentSystem() {
    return prevayler.prevalentSystem();
  }

  @Override
  public synchronized File takeSnapshot() throws Exception {
    return prevayler.takeSnapshot();
  }

  @Override
  public String toString() {
    return "Synchronized(" + prevayler.toString() + ")";
  }

}
