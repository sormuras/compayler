package org.prevayler.contrib.p8.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.Transaction;
import org.prevayler.contrib.compayler.TestTool;
import org.prevayler.contrib.p8.P8;
import org.prevayler.contrib.p8.SynchronizedPrevayler;

public class BenchConcurrent {

  static class Producer implements Callable<Producer> {

    final Prevayler<StringBuilder> prevayler;
    int operationCounter;

    Producer(Prevayler<StringBuilder> prevayler) {
      this.prevayler = prevayler;
    }

    @Override
    public Producer call() throws Exception {
      for (int i = 0; i < 1000; i++) {
        prevayler.execute((Transaction<StringBuilder>) (builder, date) -> builder.append("1"));
        operationCounter++;
      }
      return this;
    }

  }

  // private static XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
  //
  // @AfterClass
  // public static void oneTimeTearDown() throws Exception {
  // System.out.println("@AfterClass - oneTimeTearDown");
  // createChart(xySeriesCollection, "Concurrent Prevayler Implementation Benchmark");
  // }

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  // public static JFreeChart createChart(XYDataset dataset, String fileName) throws Exception {
  // final JFreeChart chart = ChartFactory.createXYLineChart(fileName, // chart title
  // "number of threads", // x axis label
  // "operations per second", // y axis label
  // dataset, // data
  // PlotOrientation.VERTICAL, true, // include legend
  // true, // tooltips
  // false // urls
  // );
  // chart.setBackgroundPaint(Color.white);
  // final XYPlot plot = chart.getXYPlot();
  // plot.setBackgroundPaint(Color.lightGray);
  // // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
  // plot.setDomainGridlinePaint(Color.white);
  // plot.setRangeGridlinePaint(Color.white);
  // final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
  // // renderer.setSeriesLinesVisible(0, false);
  // // renderer.setSeriesShapesVisible(1, false);
  // plot.setRenderer(renderer);
  // final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
  // rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
  // ChartUtilities.saveChartAsPNG(new File(fileName + ".png"), chart, 800, 600);
  // return chart;
  // }

  @Test
  public void testP8() throws Exception {
    File folder = temp.newFolder();
    // XYSeries series = new XYSeries("P8-synced");
    for (int i = 1; i <= 50; i++) {
      // if (i > 10 && i % 20 != 0)
      // continue;
      for (File file : folder.listFiles()) {
        file.delete();
      }
      System.gc();
      @SuppressWarnings("unused")
      double ops = test(new SynchronizedPrevayler<>(new P8<>(new StringBuilder(), folder, "snap", "journal", 500 * 1000 * 1000)), i);
      // series.add(i, ops);
    }
    // xySeriesCollection.addSeries(series);
  }

  @Test
  public void testPrevayler() throws Exception {
    File folder = temp.newFolder();
    // XYSeries series = new XYSeries("Prevayler");
    for (int i = 1; i <= 50; i++) {
      // if (i > 10 && i % 20 != 0)
      // continue;
      for (File file : folder.listFiles()) {
        file.delete();
      }
      System.gc();
      @SuppressWarnings("unused")
      double ops = test(TestTool.prevayler(new StringBuilder(), folder), i);
      // series.add(i, ops);
    }
    // xySeriesCollection.addSeries(series);
  }

  double test(Prevayler<StringBuilder> prevayler, int numberOfThreads) throws Exception {

    List<Producer> producers = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++)
      producers.add(new Producer(prevayler));

    ExecutorService execs = Executors.newFixedThreadPool(numberOfThreads);

    long start = System.nanoTime();
    long duration = start;
    try {
      execs.invokeAll(producers);

      execs.shutdown();
      execs.awaitTermination(1, TimeUnit.MINUTES);
      duration = System.nanoTime() - start;
    } finally {
      prevayler.close();
      execs.shutdownNow();
    }

    int operations = producers.stream().mapToInt(p -> p.operationCounter).sum();
    double opspers = ((double) operations / (double) duration) * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

    System.out.println(String.format("[%4d] %12d ops in %12d nanos: %12.2f op/s", numberOfThreads, operations, duration, opspers));

    return opspers;
  }

}
