package org.prevayler.contrib.p8;

import static java.lang.String.format;
import static java.nio.file.Files.createLink;
import static java.nio.file.Files.deleteIfExists;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.Query;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;
import org.prevayler.implementation.clock.MachineClock;

public class P8<P> implements Prevayler<P>, Closeable {

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
  private final Journal<P> journal;

  /**
   * Underlying prevalent system.
   */
  private final P prevalentSystem;

  /**
   * Number of transactions that led to current system snapshot.
   */
  private long snapshotTransactionCounter;

  /**
   * Snapshot file pointer.
   */
  private final File snapshotFile;

  public P8(P prevalentSystem, File base) throws Exception {
    this(prevalentSystem, base, "snap.shot", "sliced.journal", 100 * 1024 * 1024);
  }

  public P8(P prevalentSystem, File base, String snapshotName, String journalName, long journalSize) throws Exception {
    this.base = base;

    base.mkdirs();

    this.clock = new MachineClock();

    this.snapshotFile = new File(base, snapshotName);

    this.prevalentSystem = reloadFromSnapshot(snapshotFile, prevalentSystem);

    this.journal = new Journal<>(this, new File(base, journalName), journalSize);
  }

  public long age() {
    return snapshotTransactionCounter + journal.getMemoryTransactionCounter();
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

  public P reloadFromSnapshot(File snapshotFile, P initialPrevalentSystem) {
    if (!snapshotFile.exists())
      return initialPrevalentSystem;

    try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(snapshotFile))) {
      snapshotTransactionCounter = stream.readLong();
      @SuppressWarnings("unchecked")
      P storedSystem = (P) stream.readObject();
      return storedSystem;
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  @Override
  public File takeSnapshot() throws Exception {
    return takeSnapshot(snapshotFile).toFile();
  }

  public Path takeSnapshot(File snapshotFile) throws Exception {
    File taken = new File(base, "snap-" + Instant.now().toString().replace(':', '-') + ".shot");

    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(taken))) {
      out.writeLong(snapshotTransactionCounter = age());
      out.writeObject(prevalentSystem);
    }

    Path snapshotLink = snapshotFile.toPath();
    deleteIfExists(snapshotLink);
    createLink(snapshotLink, taken.toPath());

    journal.clear();

    return snapshotLink;
  }

  @Override
  public String toString() {
    return format("P8(%s)[age=%d,usage=%.2f%%]", prevalentSystem.getClass().getSimpleName(), age(), journal.usage());
  }

}