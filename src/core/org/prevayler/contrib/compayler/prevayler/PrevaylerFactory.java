package org.prevayler.contrib.compayler.prevayler;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
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

/**
 * Used to instantiate a prevayler implementation.
 */
@FunctionalInterface
public interface PrevaylerFactory<P> {

  public static <P> Prevayler<P> prevayler(P prevalentSystem) throws Exception {
    return prevayler(prevalentSystem, prevalentSystem.getClass().getClassLoader());
  }

  public static <P> Prevayler<P> prevayler(P prevalentSystem, ClassLoader loader) throws Exception {
    return prevayler(prevalentSystem, loader, new File("PrevalenceBase"));
  }

  public static <P> Prevayler<P> prevayler(P prevalentSystem, ClassLoader loader, File folder) throws Exception {
    PrevaylerDirectory directory = new PrevaylerDirectory(folder);
    Monitor monitor = new SimpleMonitor(System.err);
    Journal journal = new PersistentJournal(directory, 0, 0, true, "journal", monitor);
    Serializer serializer = new JavaSerializer(loader);
    Map<String, Serializer> map = Collections.singletonMap("snapshot", serializer);
    GenericSnapshotManager<P> snapshotManager = new GenericSnapshotManager<>(map, "snapshot", prevalentSystem, directory, serializer);
    Clock clock = new MachineClock();
    TransactionPublisher publisher = new CentralPublisher(clock, journal);
    boolean transactionDeepCopyMode = true;
    return new PrevaylerImpl<>(snapshotManager, publisher, serializer, transactionDeepCopyMode);
  }

  /**
   * Create Prevayler instance.
   * 
   * @param loader
   *          The class loader that must be used to (de-)serialize transaction objects.
   * @return
   */
  Prevayler<P> createPrevayler(ClassLoader loader) throws Exception;

}
