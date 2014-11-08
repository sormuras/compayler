package databench;

import static java.lang.String.format;

import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.prevayler.contrib.p8.concurrent.ReadWriteLockPrevayler;
import org.prevayler.contrib.p8.concurrent.StampedLockPrevayler;
import org.prevayler.contrib.p8.concurrent.SynchronizedPrevayler;

import databench.prevayler.P8Subject;
import databench.prevayler.PrevaylerSubject;
import de.codeturm.util.chartgo.Chart;
import de.codeturm.util.chartgo.RedirectConsole;
import de.codeturm.util.chartgo.Chart.Background;
import de.codeturm.util.chartgo.Chart.ChartType;

public class Databench {

  @FunctionalInterface
  interface Factory {
    Bank<Integer> create(File folder, int threads);
  }

  class Result {
    long entireDuration;
    final Factory factory;
    final Map<Integer, Double> map;
    final String name;
    double ops;
    long setupDuration;

    Result(String name, Factory factory) {
      this.name = name;
      this.factory = factory;
      this.map = new HashMap<>();
    }
  }

  class Worker implements Callable<Worker> {

    Bank<Integer> bank;
    String name;
    long operations = 0L;
    Random random = new Random();

    Worker(Bank<Integer> bank, String name) {
      this.bank = bank;
      this.name = name;
    }

    Integer any() {
      return random.nextInt(numberOfBankAccounts);
    }

    @Override
    public Worker call() throws Exception {

      Thread thread = Thread.currentThread();

      while (!thread.isInterrupted()) {

        if (operations % workername == 0)
          System.out.print(name);

        try {
          if (random.nextInt(100) < percentage)
            bank.transfer(any(), any(), any());
          else
            bank.getAccountStatus(any());
        } catch (RuntimeException runtimeException) {
          if (ignoreRuntimeExceptionAndBreak.get())
            break;
          runtimeException.printStackTrace();
          throw runtimeException;
        }

        operations++;

        if (workeryield)
          Thread.yield();

      }

      return this;
    }

  }

  private static final String[] NAMES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
      "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
      "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "_" };

  public static Chart createChart() {
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

  public static String get(String key, String defaultValue) {
    String value = System.getProperty(key);
    if (value != null)
      return value;
    value = System.getenv(key);
    return (value != null) ? value : defaultValue;
  }

  public static void main(String[] args) throws Exception {

    Console console = System.console();
    if (console != null) {
      console.format("Running with console!%n");
    } else if (!GraphicsEnvironment.isHeadless()) {
      RedirectConsole redirect = new RedirectConsole("Databench");
      synchronized (redirect) {
        redirect.wait(5000);
      }
    } else {
      // Put it in the log
    }

    Runtime r = Runtime.getRuntime();
    System.out.println("BEGIN");
    System.out.println(" o PROCESSOR_IDENTIFIER   : " + System.getenv("PROCESSOR_IDENTIFIER"));
    System.out.println(" o PROCESSOR_ARCHITECTURE : " + System.getenv("PROCESSOR_ARCHITECTURE"));
    System.out.println(" o NUMBER_OF_PROCESSORS   : " + System.getenv("NUMBER_OF_PROCESSORS"));
    System.out.println(" o os.name                : " + System.getProperty("os.name"));
    System.out.println(" o os.version             : " + System.getProperty("os.version"));
    System.out.println(" o os.arch                : " + System.getProperty("os.arch"));
    System.out.println(" o memory used            :  " + (r.totalMemory() - r.freeMemory()) + " bytes");
    System.out.println(" o memory free            : " + r.freeMemory() + " bytes");
    System.out.println(" o memory total           : " + r.totalMemory() + " bytes");
    System.out.println(" o memory max             : " + r.maxMemory() + " bytes");

    Databench main = new Databench();
    Chart chart = createChart();

    if (Boolean.parseBoolean(get("run.transient", "false"))) {
      chart.add(main.time("Volatile", (folder, threads) -> new P8Subject(null, threads, p -> new StampedLockPrevayler<>(p))));
      chart.add(main.time("Transient", (folder, threads) -> new PrevaylerSubject(null, threads)));
    }

    if (Boolean.parseBoolean(get("run.persistent", "true"))) {
      chart.add(main.time("P8(never)", (folder, threads) -> new P8Subject(folder, threads, Long.MAX_VALUE)));
      chart.add(main.time("P8(1 sec)", (folder, threads) -> new P8Subject(folder, threads, TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS))));
      chart.add( main.time("P8(force)", (folder, threads) -> new P8Subject(folder, threads, 0L)));
      chart.add(main.time("Prevayler", (folder, threads) -> new PrevaylerSubject(folder, threads)));
    }

    if (Boolean.parseBoolean(get("run.concurrent", "false"))) {
      chart.add(main.time("P8(synchro)", (folder, threads) -> new P8Subject(folder, 0, p -> new SynchronizedPrevayler<>(p))));
      chart.add(main.time("P8(readwrt)", (folder, threads) -> new P8Subject(folder, 0, p -> new ReadWriteLockPrevayler<>(p))));
      chart.add(main.time("P8(stamped)", (folder, threads) -> new P8Subject(folder, 0, p -> new StampedLockPrevayler<>(p))));
    }
    
    chart.go();

    System.out.println();
    System.out.println("END.");
  }

  public final AtomicBoolean ignoreRuntimeExceptionAndBreak = new AtomicBoolean(false);
  public final int numberOfBankAccounts = Integer.parseUnsignedInt(get("numberOfBankAccounts", "1000000"));
  public final int percentage = Integer.parseUnsignedInt(get("percentage", "20"));
  public final int roundsMulti = Integer.parseUnsignedInt(get("rounds.multi", "3"));
  public final int roundsSingle = Integer.parseUnsignedInt(get("rounds.single", "6"));
  public final int threadsMax = Integer.parseUnsignedInt(get("threads.max", "8"));
  public final int threadsMin = Integer.parseUnsignedInt(get("threads.min", "1"));
  public final int workername = Integer.parseUnsignedInt(get("worker.name", "50000"));
  public final int workertime = Integer.parseUnsignedInt(get("worker.time", "3"));
  public final boolean workeryield = Boolean.parseBoolean(get("worker.yield", "false"));

  public void time(Result result, int threads) throws Exception {
    int lastRound = threads == 1 ? roundsSingle : roundsMulti;
    double ops = 0d;
    for (int round = 1; round <= lastRound; round++) {
      File folder = time(result, threads, round, round == lastRound);
      ops += result.ops;
      while (folder.exists()) {
        System.gc();
        Arrays.asList(folder.listFiles()).forEach(f -> f.delete());
        if (folder.delete())
          break;
        Thread.sleep(1000);
      }
    }
    ops = ops / lastRound / 1000d;
    result.map.put(Integer.valueOf(threads), ops);
  }

  public File time(Result result, int threads, int round, boolean lastRound) throws Exception {
    Watch watch = Watch.start();

    File folder = Files.createTempDirectory("databench-" + result.name + "-" + threads + "-" + round + ".").toFile();

    Bank<Integer> bank = result.factory.create(folder, threads);

    bank.setUp(numberOfBankAccounts);
    result.setupDuration = watch.nanosEllapsed();

    ExecutorService execs = Executors.newFixedThreadPool(threads);
    ArrayList<Worker> workers = new ArrayList<>(threads);
    for (int i = 0; i < threads; i++)
      workers.add(new Worker(bank, NAMES[26 + i % NAMES.length]));

    long operations = 0L;
    long duration = Long.MAX_VALUE;
    try {
      long start = System.nanoTime();

      System.out.print(format("#%s%3d %2d (", lastRound ? "_" : " ", threads, round));
      execs.invokeAll(workers, workertime, TimeUnit.SECONDS);
      ignoreRuntimeExceptionAndBreak.set(true);
      System.out.print(")");

      duration = System.nanoTime() - start;

      for (int i = 0; i < threads; i++)
        operations += workers.get(i).operations;

    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      bank.tearDown();
      if (!execs.shutdownNow().isEmpty()) {
        throw new Error("At least one task is awaiting execution?!");
      }
    }

    result.ops = (double) operations / duration * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

    System.out.println(format("%12.2f op/s", result.ops));

    return folder;
  }

  public Chart.Group time(String name, Factory factory) throws Exception {
    Result result = new Result(name, factory);
    System.out.println();
    System.out.println("#########################################################################");
    System.out.println("#   t");
    System.out.println("#   h  r  +-" + new String(new char[name.length()]).replace("\0", "-") + "-+");
    System.out.println("#   r  o  | " + name + " |");
    System.out.println("#   e  u  +-" + new String(new char[name.length()]).replace("\0", "-") + "-+");
    System.out.println("#   a  n ");
    System.out.println("#_  d  d (one character is a block of 1-" + workername + " operations)");
    Watch watch = Watch.start();
    Chart.Group group = new Chart.Group(name);
    for (int threads = threadsMin; threads <= threadsMax; threads++) {
      time(result, threads);
      System.out.println("#      average " + result.map.get(Integer.valueOf(threads)) + " op/s");
      group.points.add(new Chart.Point(Integer.valueOf(threads), result.map.get(Integer.valueOf(threads))));
    }
    result.entireDuration = watch.nanosEllapsed();
    System.out.println("#");
    System.out.println("#  Result for " + name);
    System.out.println("#   o entire duration: " + result.entireDuration);
    System.out.println("#   o  setup duration: " + result.setupDuration);
    System.out.println("#########################################################################");
    return group;
  }

}
