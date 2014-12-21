package org.prevayler.contrib.p8.benchmark;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.P8;
import org.prevayler.contrib.p8.concurrent.StampedLockPrevayler;
import org.prevayler.contrib.p8.util.VolatilePrevayler;
import org.prevayler.foundation.monitor.Monitor;
import org.prevayler.foundation.monitor.SimpleMonitor;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.PrevaylerDirectory;
import org.prevayler.implementation.PrevaylerImpl;
import org.prevayler.implementation.clock.MachineClock;
import org.prevayler.implementation.journal.Journal;
import org.prevayler.implementation.journal.PersistentJournal;
import org.prevayler.implementation.journal.TransientJournal;
import org.prevayler.implementation.publishing.CentralPublisher;
import org.prevayler.implementation.publishing.TransactionPublisher;
import org.prevayler.implementation.snapshot.GenericSnapshotManager;
import org.prevayler.implementation.snapshot.NullSnapshotManager;

import de.codeturm.util.chartgo.Chart;
import de.codeturm.util.chartgo.Chart.Background;
import de.codeturm.util.chartgo.Chart.ChartType;

public class Benchmark {

  public static String get(String key, String defaultValue) {
    String value = System.getProperty(key);
    if (value != null)
      return value;
    value = System.getenv(key);
    return (value != null) ? value : defaultValue;
  }

  public static void main(String[] args) throws Exception {
    Benchmark benchmark = new Benchmark();

    benchmark.test("Prevayler (Default)", (builder, folder, numberOfThreads) -> prevayler(builder, folder), false);

    benchmark.test("Prevayler (Transient)", (builder, folder, numberOfThreads) -> prevaylerTransient(builder), false);

    benchmark.test("P8 (raw/StampLock)", (builder, folder, numberOfThreads) -> {
      P8<StringBuilder> p8 = new P8<>(builder, folder, 10 * 1000 * 1000, Long.MAX_VALUE);
      return numberOfThreads == 1 ? p8 : new StampedLockPrevayler<>(p8);
    }, true);

    benchmark.test("Volatile (Transient)", (builder, folder, numberOfThreads) -> new VolatilePrevayler<>(builder), false);

    benchmark.chart.go();
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
    Journal journal = new PersistentJournal(directory, 0, 0, false, "journal", monitor);
    Serializer serializer = new JavaSerializer(loader);
    Map<String, Serializer> map = Collections.singletonMap("snapshot", serializer);
    GenericSnapshotManager<P> snapshotManager = new GenericSnapshotManager<>(map, "snapshot", prevalentSystem, directory, serializer);
    Clock clock = new MachineClock();
    TransactionPublisher publisher = new CentralPublisher(clock, journal);
    boolean transactionDeepCopyMode = false;
    return new PrevaylerImpl<>(snapshotManager, publisher, serializer, transactionDeepCopyMode);
  }

  /**
   * @return default prevayler using given class loader and given journaling folder
   */
  public static <P> Prevayler<P> prevaylerTransient(P prevalentSystem) throws Exception {
    Journal journal = new TransientJournal();
    Serializer serializer = new JavaSerializer(prevalentSystem.getClass().getClassLoader());
    GenericSnapshotManager<P> snapshotManager = new NullSnapshotManager<P>(prevalentSystem, "No snap, no bite.");
    Clock clock = new MachineClock();
    TransactionPublisher publisher = new CentralPublisher(clock, journal);
    boolean transactionDeepCopyMode = false;
    return new PrevaylerImpl<>(snapshotManager, publisher, serializer, transactionDeepCopyMode);
  }

  private final Chart chart;
  private final boolean consistency;
  private final File folder;
  private final int rounds;
  private final int threadsMax;
  private final int threadsMin;
  private final int threadsSkipStart;
  private final int threadsSkipStep;
  private final int warmup;

  public Benchmark() {
    this.chart = createChart();
    this.folder = createFolder(get("folder", ""));
    this.consistency = Boolean.parseBoolean(get("consistency", "true"));
    this.rounds = Integer.parseUnsignedInt(get("rounds", "10"));
    this.warmup = Integer.parseUnsignedInt(get("warmup", "3"));
    this.threadsMin = Integer.parseUnsignedInt(get("threads.min", "1"));
    this.threadsMax = Integer.parseUnsignedInt(get("threads.max", "80"));
    this.threadsSkipStart = Integer.parseUnsignedInt(get("threads.skip.start", "16"));
    this.threadsSkipStep = Integer.parseUnsignedInt(get("threads.skip.step", "10"));
  }

  private Chart createChart() {
    Chart chart = new Chart();
    chart.setChartType(ChartType.LINE);
    chart.setTitle("P8 Benchmark");
    chart.setTitleX("concurrent threads");
    chart.setTitleY("op/s (div 1000)");
    chart.setLegend(true);
    chart.setBackground(Background.GRADIENTGRAY);
    chart.setGridlines(true);
    return chart;
  }

  private File createFolder(String name) {
    if (name.isEmpty())
      try {
        return Files.createTempDirectory("simple").toFile();
      } catch (IOException e) {
        throw new Error("Can not create temp directory!", e);
      }
    File folder = new File(name);
    folder.mkdirs();
    return folder;
  }

  public double test(Prevayler<StringBuilder> prevayler, int numberOfThreads) throws Exception {

    List<Producer> producers = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++)
      producers.add(new Producer(prevayler));

    ExecutorService execs = Executors.newFixedThreadPool(numberOfThreads);
    CompletionService<Producer> completionService = new ExecutorCompletionService<>(execs);

    int remainingFutures = numberOfThreads;
    long duration = Long.MAX_VALUE;
    try {
      long start = System.nanoTime();

      producers.forEach(completionService::submit);

      while (remainingFutures > 0) {
        completionService.take().get();
        remainingFutures--;
      }

      duration = System.nanoTime() - start;

    } finally {
      execs.shutdownNow();
      prevayler.close();
    }

    double operations = producers.stream().mapToDouble(p -> p.getOperationCounter()).sum();
    double opspers = operations / duration * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

    String message = "[%4d] %12.0f ops in %12d nanos: %12.2f op/s - %s";
    System.out.println(format(message, numberOfThreads, operations, duration, opspers, prevayler));

    return opspers;
  }

  public Chart.Group test(String name, Creator creator, boolean persistent) throws Exception {
    Chart.Group dataGroup = new Chart.Group(name);
    StringBuilder builder = new StringBuilder(1000 * 1000);

    for (int numberOfThreads = threadsMin; numberOfThreads <= threadsMax; numberOfThreads++) {
      if (numberOfThreads > threadsSkipStart && numberOfThreads % threadsSkipStep != 0)
        continue;

      double ops = 0d;
      for (int round = 0; round < warmup + rounds; round++) {
        builder.setLength(0);
        while (folder.exists()) {
          System.gc();
          Arrays.asList(folder.listFiles()).forEach(f -> f.delete());
          if (folder.delete())
            break;
          Thread.sleep(1000);
        }

        Prevayler<StringBuilder> prevayler = creator.create(builder, folder, numberOfThreads);
        assert prevayler != null;

        double result = test(prevayler, numberOfThreads);

        if (consistency && persistent) {
          String expectedString = prevayler.prevalentSystem().toString();
          try {
            prevayler = creator.create(new StringBuilder(), folder, numberOfThreads);
            String actualString = prevayler.prevalentSystem().toString();
            if (expectedString.hashCode() != actualString.hashCode()) {
              System.err.println("Hash code mismatch checking consistency!");
              System.err.println(expectedString.hashCode() + " != " + actualString.hashCode());
              System.err.println(expectedString.length() + " ?= " + actualString.length());
            }
            // System.out.println("      expected: " + expectedString.substring(0, Math.min(80, expectedString.length())) + "...");
            // System.out.println("        actual: " + actualString.substring(0, Math.min(80, actualString.length())) + "...");
          } finally {
            prevayler.close();
          }
        }

        if (round < warmup)
          continue;

        ops += result;
      }

      dataGroup.points.add(new Chart.Point(numberOfThreads, Math.floor(ops / rounds / 1000)));
    }

    chart.getGroups().add(dataGroup);

    return dataGroup;
  }

}
