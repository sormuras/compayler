package org.prevayler.contrib.compayler;

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

public class TestTool {

  @SuppressWarnings("unchecked")
  public static <T> T decorate(Class<T> interfaceClass, T prevalentSystem, File folder) throws Exception {
    Compayler compayler = new Compayler(interfaceClass);
    Prevayler<T> prevayler = prevayler(prevalentSystem, folder);
    return (T) Class.forName(compayler.getDecoratorName()).getConstructor(Prevayler.class).newInstance(prevayler);
  }

  /**
   * @return default prevayler using prevalent system class loader and given journaling folder
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem, File folder) throws Exception {
    return prevayler(prevalentSystem, folder, prevalentSystem.getClass().getClassLoader());
  }

  /**
   * @return default prevayler using given class loader and given journaling folder
   */
  public static <P> Prevayler<P> prevayler(P prevalentSystem, File folder, ClassLoader loader) throws Exception {
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

}
