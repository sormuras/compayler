package databench;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.prevayler.contrib.p8.ReadWriteLockPrevayler;
import org.prevayler.contrib.p8.StampedLockPrevayler;
import org.prevayler.contrib.p8.SynchronizedPrevayler;

import databench.prevayler.P8Subject;
import databench.prevayler.PrevaylerSubject;

public class Databench {

  @FunctionalInterface
  interface Factory {
    Bank<Integer> create(File folder, int threads);
  }

  class Result {
    long setupDuration;
    long entireDuration;
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

        if (yield)
          Thread.yield();

      }

      return this;
    }

  }

  private static final String[] NAMES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
      "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
      "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "_" };

  public static void main(String[] args) throws Exception {
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

    if (Boolean.parseBoolean(System.getProperty("run.transient", "false"))) {
      main.time("Volatile", (folder, threads) -> new P8Subject(null, threads, p -> new StampedLockPrevayler<>(p)));
      main.time("Transient", (folder, threads) -> new PrevaylerSubject(null, threads));
    }

    if (Boolean.parseBoolean(System.getProperty("run.persistent", "true"))) {
      main.time("P8(never)", (folder, threads) -> new P8Subject(folder, threads, Long.MAX_VALUE));
      main.time("P8(1 sec)", (folder, threads) -> new P8Subject(folder, threads, TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)));
      main.time("P8(force)", (folder, threads) -> new P8Subject(folder, threads, 0L));
      main.time("Prevayler", (folder, threads) -> new PrevaylerSubject(folder, threads));
    }
    
    if (Boolean.parseBoolean(System.getProperty("run.concurrent", "false"))) {
      main.time("P8(synchro)", (folder, threads) -> new P8Subject(folder, 0, p -> new SynchronizedPrevayler<>(p)));
      main.time("P8(readwrt)", (folder, threads) -> new P8Subject(folder, 0, p -> new ReadWriteLockPrevayler<>(p)));
      main.time("P8(stamped)", (folder, threads) -> new P8Subject(folder, 0, p -> new StampedLockPrevayler<>(p)));
    }

    System.out.println();
    System.out.println("END.");
  }

  public final int numberOfBankAccounts = Integer.parseUnsignedInt(System.getProperty("numberOfBankAccounts", "1000000"));
  public final Result result = new Result();
  public final boolean yield = Boolean.parseBoolean(System.getProperty("yield", "false"));
  public final AtomicBoolean ignoreRuntimeExceptionAndBreak = new AtomicBoolean(false);
  public final int threadsMax = Integer.parseUnsignedInt(System.getProperty("threads.max", "8"));
  public final int threadsMin = Integer.parseUnsignedInt(System.getProperty("threads.min", "1"));
  public final int percentage = Integer.parseUnsignedInt(System.getProperty("percentage", "20"));
  public final int roundsSingle = Integer.parseUnsignedInt(System.getProperty("rounds.single", "6"));
  public final int roundsMulti = Integer.parseUnsignedInt(System.getProperty("rounds.multi", "3"));
  public final int workername = Integer.parseUnsignedInt(System.getProperty("worker.name", "50000"));
  public final int workertime = Integer.parseUnsignedInt(System.getProperty("worker.time", "3"));

  public void time(String name, Factory factory) throws Exception {
    System.out.println();
    System.out.println("#########################################################################");
    System.out.println("#   t");
    System.out.println("#   h  r  +-" + new String(new char[name.length()]).replace("\0", "-") + "-+");
    System.out.println("#   r  o  | " + name + " |");
    System.out.println("#   e  u  +-" + new String(new char[name.length()]).replace("\0", "-") + "-+");
    System.out.println("#   a  n ");
    System.out.println("#_  d  d (one character is a block of 1-" + workername + " operations)");
    Watch watch = Watch.start();
    for (int threads = threadsMin; threads <= threadsMax; threads++) {
      time(name, factory, threads);
    }
    result.entireDuration = watch.nanosEllapsed();
    System.out.println("#");
    System.out.println("#  Result for " + name);
    System.out.println("#   o entire duration: " + result.entireDuration);
    System.out.println("#   o  setup duration: " + result.setupDuration);
    System.out.println("#########################################################################");
  }

  public void time(String name, Factory factory, int threads) throws Exception {
    int lastRound = threads == 1 ? roundsSingle : roundsMulti;
    for (int round = 1; round <= lastRound; round++) {
      File folder = time(name, factory, threads, round, round == lastRound);

      while (folder.exists()) {
        System.gc();
        Arrays.asList(folder.listFiles()).forEach(f -> f.delete());
        if (folder.delete())
          break;
        Thread.sleep(1000);
      }
    }
  }

  public File time(String name, Factory factory, int threads, int round, boolean lastRound) throws Exception {
    Watch watch = Watch.start();

    File folder = Files.createTempDirectory("databench-" + name + "-" + threads + "-" + round + ".").toFile();

    Bank<Integer> bank = factory.create(folder, threads);

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

    double opspers = (double) operations / duration * TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);

    System.out.println(format("%12.2f op/s", opspers));

    return folder;
  }

}
