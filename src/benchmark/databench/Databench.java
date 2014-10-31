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

        if (operations % 50000 == 0)
          System.out.print(name);

        try {
          if (random.nextBoolean() && random.nextBoolean())
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
    System.out.println(System.getenv("PROCESSOR_IDENTIFIER"));
    System.out.println(System.getenv("PROCESSOR_ARCHITECTURE"));
    System.out.println(System.getenv("NUMBER_OF_PROCESSORS"));
    System.out.println(System.getProperty("os.name"));
    System.out.println(System.getProperty("os.version"));
    System.out.println(System.getProperty("os.arch"));

    Databench main = new Databench();

    main.time("P8(never)", (folder, threads) -> new P8Subject(folder, threads, Long.MAX_VALUE));
    main.time("P8(1 sec)", (folder, threads) -> new P8Subject(folder, threads, TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)));
    main.time("P8(force)", (folder, threads) -> new P8Subject(folder, threads, 0L));
    main.time("Prevayler", (folder, threads) -> new PrevaylerSubject(folder, threads));
  }

  public final int numberOfBankAccounts = Integer.parseUnsignedInt(System.getProperty("numberOfBankAccounts", "10000"));
  public final Result result = new Result();
  public final boolean yield = Boolean.parseBoolean(System.getProperty("yield", "true"));
  public final AtomicBoolean ignoreRuntimeExceptionAndBreak = new AtomicBoolean(false);

  public void time(String name, Factory factory) throws Exception {
    Watch watch = Watch.start();

    for (int threads = 1; threads <= 2; threads++) {
      System.out.println("\n" + threads + " thread(s)...");
      time(name, factory, threads);
    }

    result.entireDuration = watch.nanosEllapsed();
    System.out.println("#");
    System.out.println("#  Result for " + name);
    System.out.println("#   o entire duration: " + result.entireDuration);
    System.out.println("#   o  setup duration: " + result.setupDuration);
    System.out.println("#");
  }

  public void time(String name, Factory factory, int threads) throws Exception {
    for (int round = 1; round <= (threads == 1 ? 2 : 1); round++) {
      File folder = time(name, factory, threads, round);
      Arrays.asList(folder.listFiles()).forEach(System.out::println);
      Thread.sleep(1500);
      System.gc();
      Thread.sleep(1500);
      System.gc();
      Thread.sleep(1500);
      System.gc();
      Thread.sleep(1500);
      for (File f : folder.listFiles())
        Files.delete(f.toPath());
      Arrays.asList(folder.listFiles()).forEach(System.out::println);
      Files.deleteIfExists(folder.toPath());
    }
  }

  public File time(String name, Factory factory, int threads, int round) throws Exception {
    Watch watch = Watch.start();

    File folder = Files.createTempDirectory("databench-" + name + "-" + threads + "-" + round + ".").toFile();

    Bank<Integer> bank = factory.create(folder, threads);

    bank.setUp(numberOfBankAccounts);
    result.setupDuration = watch.nanosEllapsed();

    ExecutorService execs = Executors.newFixedThreadPool(threads);
    ArrayList<Worker> workers = new ArrayList<>(threads);
    for (int i = 0; i < threads; i++)
      workers.add(new Worker(bank, NAMES[i % NAMES.length]));

    long operations = 0L;
    long duration = Long.MAX_VALUE;
    try {
      long start = System.nanoTime();

      System.out.print("(");
      execs.invokeAll(workers, 5, TimeUnit.SECONDS);
      ignoreRuntimeExceptionAndBreak.set(true);
      System.out.println(")");

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

    String message = "[%4d] %12d ops in %12d nanos: %12.2f op/s - %s";
    System.out.println(format(message, threads, operations, duration, opspers, bank));

    return folder;
  }

}
