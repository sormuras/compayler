package databench.prevayler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.prevayler.Prevayler;
import org.prevayler.contrib.p8.P8;
import org.prevayler.contrib.p8.SynchronizedPrevayler;
import org.prevayler.contrib.p8.VolatilePrevayler;

import databench.AccountStatus;
import databench.Bank;

public class P8Subject implements Bank<Integer> {

  private static final long serialVersionUID = 1L;

  private final Prevayler<Map<Integer, PrevaylerAccount>> prevayler;

  public P8Subject(File folder, int threads, long flushNanos) {
    try {
      Map<Integer, PrevaylerAccount> prevalentSystem = new HashMap<>();
      if (folder == null) {
        VolatilePrevayler<Map<Integer, PrevaylerAccount>> vola = new VolatilePrevayler<>(prevalentSystem, false, null);
        this.prevayler = threads == 1 ? vola : new SynchronizedPrevayler<>(vola);
      } else {
        P8<Map<Integer, PrevaylerAccount>> p8 = new P8<>(prevalentSystem, folder, 100 * 1024 * 1024, flushNanos);
        this.prevayler = threads == 1 ? p8 : new SynchronizedPrevayler<>(p8);
      }
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
