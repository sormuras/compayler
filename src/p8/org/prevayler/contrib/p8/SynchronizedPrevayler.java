package org.prevayler.contrib.p8;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

public class SynchronizedPrevayler<P> implements Prevayler<P>, AutoCloseable {

  private final ReadWriteLock lock;
  private final Prevayler<P> prevayler;

  public SynchronizedPrevayler(Prevayler<P> prevayler) throws Exception {
    this(prevayler, new ReentrantReadWriteLock());
  }

  public SynchronizedPrevayler(Prevayler<P> prevayler, ReadWriteLock lock) throws Exception {
    this.prevayler = prevayler;
    this.lock = lock;
  }

  @Override
  public Clock clock() {
    return prevayler.clock();
  }

  @Override
  public void close() throws IOException {
    prevayler.close();
  }

  @Override
  public void execute(Transaction<? super P> transaction) {
    lock.writeLock().lock();
    try {
      prevayler.execute(transaction);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public <R> R execute(Query<? super P, R> query) throws Exception {
    lock.readLock().lock();
    try {
      return prevayler.execute(query);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    lock.writeLock().lock();
    try {
      return prevayler.execute(transactionWithQuery);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public <R> R execute(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
    lock.writeLock().lock();
    try {
      return prevayler.execute(sureTransactionWithQuery);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public P prevalentSystem() {
    return prevayler.prevalentSystem();
  }

  @Override
  public File takeSnapshot() throws Exception {
    lock.readLock().lock();
    try {
      return prevayler.takeSnapshot();
    } finally {
      lock.readLock().unlock();
    }
  }
  
  @Override
  public String toString() {
    return "Synchronized(" + prevayler.toString() + ")";
  }

}
