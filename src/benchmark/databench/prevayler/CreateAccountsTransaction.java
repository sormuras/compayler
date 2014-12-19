package databench.prevayler;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import org.prevayler.SureTransactionWithQuery;
import org.prevayler.contrib.p8.util.Stashable;

public class CreateAccountsTransaction implements SureTransactionWithQuery<Map<Integer, PrevaylerAccount>, Integer[]>, Stashable {

  private static final long serialVersionUID = 1L;

  private final Integer numberOfAccounts;

  public CreateAccountsTransaction(ByteBuffer source) {
    this.numberOfAccounts = Integer.valueOf(source.getInt());
  }

  public CreateAccountsTransaction(Integer numberOfAccounts) {
    this.numberOfAccounts = numberOfAccounts;
  }

  @Override
  public Integer[] executeAndQuery(Map<Integer, PrevaylerAccount> accounts, Date time) {
    Integer[] ids = createIds();
    for (Integer id : ids)
      accounts.put(id, new PrevaylerAccount());
    return ids;
  }

  private Integer[] createIds() {
    Integer[] ids = new Integer[numberOfAccounts];
    for (int i = 0; i < numberOfAccounts; i++)
      ids[i] = i;
    return ids;
  }

  @Override
  public ByteBuffer stash(ByteBuffer target) {
    target.putInt(numberOfAccounts);
    return target;
  }

}