package databench.prevayler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.P8;
import org.prevayler.contrib.p8.VolatilePrevayler;
import org.prevayler.contrib.p8.concurrent.StampedLockPrevayler;

import databench.AccountStatus;
import databench.Bank;

public class P8Subject implements Bank<Integer> {

  @FunctionalInterface
  public interface Wrapper {
    Prevayler<Map<Integer, PrevaylerAccount>> warp(Prevayler<Map<Integer, PrevaylerAccount>> prevayler);
  }

  private static final long serialVersionUID = 1L;

  private final Prevayler<Map<Integer, PrevaylerAccount>> prevayler;

  public P8Subject(File folder, int threads, long flushNanos) {
    this(folder, threads, flushNanos, StampedLockPrevayler<Map<Integer, PrevaylerAccount>>::new);
  }

  public P8Subject(File folder, int threads, Wrapper wrapper) {
    this(folder, threads, P8.DEFAULT_NANOS_BETWEEN_FORCE_FLUSH, wrapper);
  }

  public P8Subject(File folder, int threads, long flushNanos, Wrapper wrapper) {
    try {
      Map<Integer, PrevaylerAccount> map = new HashMap<>();
      Prevayler<Map<Integer, PrevaylerAccount>> prevayler = null;
      prevayler = folder == null ? new VolatilePrevayler<>(map, false, null) : new P8<>(map, folder, 100 * 1024 * 1024, flushNanos);
      if (threads == 0) {
        this.prevayler = wrapper.warp(prevayler);
        return;
      }
      this.prevayler = threads == 1 ? prevayler : wrapper.warp(prevayler);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String additionalVMParameters(boolean forMultipleVMs) {
    return "";
  }

  public AccountStatus getAccountStatus(Integer id) {
    try {
      return prevayler.execute(new GetAccountStatusQuery(id));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Integer[] setUp(Integer numberOfAccounts) {
    return prevayler.execute(new CreateAccountsTransaction(numberOfAccounts));
  }

  public void tearDown() {
    try {
      prevayler.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void transfer(Integer from, Integer to, int amount) {
    prevayler.execute(new TransferTransaction(from, to, amount));
  }

  @Override
  public String toString() {
    return prevayler.toString();
  }

  @Override
  public void warmUp() {
  }

}
