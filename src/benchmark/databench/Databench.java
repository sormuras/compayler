package databench;

import static java.lang.String.format;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        } catch (RuntimeException throwable) {
          break;
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
    Databench main = new Databench();

    main.time((folder, threads) -> new P8Subject(folder, threads, Long.MAX_VALUE));
    main.time((folder, threads) -> new P8Subject(folder, threads, TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)));
    main.time((folder, threads) -> new P8Subject(folder, threads, 0L));
    main.time((folder, threads) -> new PrevaylerSubject(folder, threads));
  }

  public final int numberOfBankAccounts = Integer.parseUnsignedInt(System.getProperty("numberOfBankAccounts", "10000"));
  public final Result result = new Result();
  public final boolean yield = Boolean.parseBoolean(System.getProperty("yield", "true"));

  public void time(Factory factory) throws Exception {
    Watch watch = Watch.start();

    Bank<Integer> bank = null;

    for (int threads = 1; threads <= 4; threads++) {
      System.out.println("\n" + threads + " thread(s)...");
      bank = time(factory, threads);
    }

    result.entireDuration = watch.nanosEllapsed();
    System.out.println("#  Result for " + bank + " (" + bank.getClass().getSimpleName() + ")");
    System.out.println("#   o entire duration: " + result.entireDuration);
    System.out.println("#   o  setup duration: " + result.setupDuration);
  }

  public Bank<Integer> time(Factory factory, int threads) throws Exception {
    Bank<Integer> bank = null;
    for (int round = 1; round <= (threads == 1 ? 8 : 1); round++) {
      bank = time(factory, threads, round);
    }
    return bank;
  }

  public Bank<Integer> time(Factory factory, int threads, int round) throws Exception {
    Watch watch = Watch.start();

    File folder = Files.createTempDirectory("databen.ch").toFile();
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

    return bank;
  }

}
