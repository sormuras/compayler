package org.prevayler.contrib.p8.concurrent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.StampedLock;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

public class StampedLockPrevayler<P> implements Prevayler<P>, AutoCloseable {

  private final StampedLock lock;
  private final Prevayler<P> prevayler;

  public StampedLockPrevayler(Prevayler<P> prevayler) {
    this(prevayler, new StampedLock());
  }

  public StampedLockPrevayler(Prevayler<P> prevayler, StampedLock lock) {
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
    long stamp = lock.writeLock();
    try {
      prevayler.execute(transaction);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public <R> R execute(Query<? super P, R> query) throws Exception {
    long stamp = lock.tryOptimisticRead();
    R result = prevayler.execute(query);
    if (lock.validate(stamp))
      return result;
    stamp = lock.readLock();
    try {
      return prevayler.execute(query);
    } finally {
      lock.unlock(stamp);
    }
  }

  @Override
  public <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    long stamp = lock.writeLock();
    try {
      return prevayler.execute(transactionWithQuery);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public <R> R execute(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
    long stamp = lock.writeLock();
    try {
      return prevayler.execute(sureTransactionWithQuery);
    } finally {
      lock.unlockWrite(stamp);
    }
  }

  @Override
  public P prevalentSystem() {
    return prevayler.prevalentSystem();
  }

  @Override
  public File takeSnapshot() throws Exception {
    long stamp = lock.readLock();
    try {
      return prevayler.takeSnapshot();
    } finally {
      lock.unlock(stamp);
    }
  }
  
  @Override
  public String toString() {
    return "StampedLock(" + prevayler.toString() + ")";
  }

}
