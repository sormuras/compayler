package org.prevayler.contrib.p8.benchmark;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.prevayler.Prevayler;
import org.prevayler.contrib.compayler.TestTool;
import org.prevayler.contrib.p8.P8;
import org.prevayler.contrib.p8.StampedLockPrevayler;
import org.prevayler.contrib.p8.VolatilePrevayler;

import de.codeturm.util.chartgo.Chart;
import de.codeturm.util.chartgo.Chart.Background;
import de.codeturm.util.chartgo.Chart.ChartType;

public class Benchmark {

  public static void main(String[] args) throws Exception {
    Benchmark benchmark = new Benchmark();

    benchmark.test("Prevayler (Default)", (builder, folder, numberOfThreads) -> TestTool.prevayler(builder, folder), false);

    benchmark.test("Prevayler (Transient)", (builder, folder, numberOfThreads) -> TestTool.prevaylerTransient(builder), false);

    benchmark.test("P8 (raw/StampLock)", (builder, folder, numberOfThreads) -> {
      P8<StringBuilder> p8 = new P8<>(builder, folder, 10 * 1000 * 1000, Long.MAX_VALUE);
      return numberOfThreads == 1 ? p8 : new StampedLockPrevayler<>(p8);
    }, true);

    benchmark.test("Volatile (Transient)", (builder, folder, numberOfThreads) -> new VolatilePrevayler<>(builder), false);

    benchmark.chart.go();
  }

  private final Chart chart;
  private final boolean consistency;
  private final File folder;
  private final int rounds;
  private final int warmup;
  private final int threadsMax;
  private final int threadsMin;
  private final int threadsSkipStart;
  private final int threadsSkipStep;

  public Benchmark() {
    this.chart = createChart();
    this.folder = createFolder(System.getProperty("folder", ""));
    this.consistency = Boolean.parseBoolean(System.getProperty("consistency", "true"));
    this.rounds = Integer.parseUnsignedInt(System.getProperty("rounds", "10"));
    this.warmup = Integer.parseUnsignedInt(System.getProperty("warmup", "3"));
    this.threadsMin = Integer.parseUnsignedInt(System.getProperty("threads.min", "1"));
    this.threadsMax = Integer.parseUnsignedInt(System.getProperty("threads.max", "80"));
    this.threadsSkipStart = Integer.parseUnsignedInt(System.getProperty("threads.skip.start", "16"));
    this.threadsSkipStep = Integer.parseUnsignedInt(System.getProperty("threads.skip.step", "10"));
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
        completionService.take();
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

}
