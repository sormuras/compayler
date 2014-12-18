package org.prevayler.contrib.p8;

import static java.lang.String.format;
import static org.prevayler.contrib.p8.util.Serialization.toPrevalentSystem;
import static org.prevayler.contrib.p8.util.Serialization.toSnapshot;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.contrib.p8.StashingJournal.MemoryHolder;
import org.prevayler.implementation.clock.MachineClock;

public class P8<P> implements Prevayler<P>, Closeable {

  /**
   * By default, force flush journal every 11 seconds.
   */
  public static final long DEFAULT_NANOS_BETWEEN_FORCE_FLUSH = TimeUnit.NANOSECONDS.convert(11L, TimeUnit.SECONDS);

  /**
   * Base folder for snapshot and journal files.
   */
  private final File base;

  /**
   * Time-teller.
   */
  private final Clock clock;

  /**
   * Journaler.
   */
  private final J8 journal;

  /**
   * Underlying prevalent system.
   */
  private final P prevalentSystem;

  /**
   * Number of transactions that led to current system snapshot.
   */
  private long snapshotAge;

  /**
   * Snapshot file pointer.
   */
  private final File snapshotFile;

  public P8(P prevalentSystem, File base) throws Exception {
    this(prevalentSystem, base, 100 * 1024 * 1024, DEFAULT_NANOS_BETWEEN_FORCE_FLUSH);
  }

  public P8(P prevalentSystem, File base, boolean stash) throws Exception {
    this(prevalentSystem, base, 100 * 1024 * 1024, DEFAULT_NANOS_BETWEEN_FORCE_FLUSH, stash);
  }

  public P8(P prevalentSystem, File base, long journalSize, long journalNanos) throws Exception {
    this(prevalentSystem, base, journalSize, journalNanos, false);
  }

  public P8(P prevalentSystem, File base, long journalSize, long journalNanos, boolean stash) throws Exception {
    this.base = base;

    base.mkdirs();

    this.clock = new MachineClock();

    this.snapshotFile = new File(base, "snap.shot");

    this.prevalentSystem = toPrevalentSystem(snapshotFile, prevalentSystem, null, i -> snapshotAge = i.readLong());

    if (stash) {
      MemoryHolder holder = new MemoryHolder(this, new File(base, "sliced.journal"), journalSize);
      this.journal = new StashingJournal(holder);
    } else {
      this.journal = new Journal<>(this, new File(base, "sliced.journal"), journalSize, journalNanos);
    }

  }

  public long age() {
    return snapshotAge + journal.getAge();
  }

  @Override
  public Clock clock() {
    return clock;
  }

  @Override
  public void close() throws IOException {
    journal.close();
  }

  @Override
  public <R> R execute(Query<? super P, R> sensitiveQuery) throws Exception {
    return sensitiveQuery.query(prevalentSystem, clock().time()); // no copy needed - queries are transient by contract
  }

  @Override
  public <R> R execute(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
    try {
      return execute((TransactionWithQuery<? super P, R>) sureTransactionWithQuery);
    } catch (RuntimeException runtime) {
      throw runtime;
    } catch (Exception checked) {
      throw new RuntimeException("Unexpected exception thrown!", checked);
    }
  }

  @Override
  public void execute(Transaction<? super P> transaction) {
    Date date = clock().time();
    Transaction<? super P> copy = journal.copy(transaction, date.getTime());
    copy.executeOn(prevalentSystem, date);
    journal.commit();
  }

  @Override
  public <R> R execute(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    Date date = clock().time();
    TransactionWithQuery<? super P, R> copy = journal.copy(transactionWithQuery, date.getTime());
    R result = copy.executeAndQuery(prevalentSystem, date);
    journal.commit();
    return result;
  }

  /**
   * Convenient wrapper for lambda-style calls.
   */
  public <R> R executeQuery(Query<? super P, R> sensitiveQuery) throws Exception {
    return execute(sensitiveQuery);
  }

  /**
   * Convenient wrapper for lambda-style calls.
   */
  public <R> R executeSureTransactionWithQuery(SureTransactionWithQuery<? super P, R> sureTransactionWithQuery) {
    return execute(sureTransactionWithQuery);
  }

  /**
   * Convenient wrapper for lambda-style calls.
   */
  public void executeTransaction(Transaction<? super P> transaction) {
    execute(transaction);
  }

  /**
   * Convenient wrapper for lambda-style calls.
   */
  public <R> R executeTransactionWithQuery(TransactionWithQuery<? super P, R> transactionWithQuery) throws Exception {
    return execute(transactionWithQuery);
  }

  /**
   * Just execute the transaction. Do <b>not</b> persist it. Discard any result.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void executeVolatile(Object object, long millisecondsSince1970) throws Exception {
    Date executionTime = new Date(millisecondsSince1970);
    if (object instanceof Transaction) {
      ((Transaction) object).executeOn(prevalentSystem, executionTime);
      return;
    }
    ((TransactionWithQuery) object).executeAndQuery(prevalentSystem, executionTime);
  }

  @Override
  public P prevalentSystem() {
    return prevalentSystem;
  }

  @Override
  public File takeSnapshot() throws Exception {
    Path snapshot = toSnapshot(prevalentSystem, base, snapshotFile, out -> out.writeLong(snapshotAge = age()));
    journal.clear();
    return snapshot.toFile();
  }

  @Override
  public String toString() {
    return format("P8(%s)[age=%d,usage=%.2f%%]", prevalentSystem.getClass().getSimpleName(), age(), journal.usage());
  }

}