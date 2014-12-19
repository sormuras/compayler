package databench.prevayler;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

import org.prevayler.Transaction;
import org.prevayler.contrib.p8.util.Stashable;

public final class TransferTransaction implements Transaction<Map<Integer, PrevaylerAccount>>, Stashable {

  private static final long serialVersionUID = 1L;

  final Integer from;
  final Integer to;
  final int amount;

  public TransferTransaction(ByteBuffer source) {
    this.from = Integer.valueOf(source.getInt());
    this.to = Integer.valueOf(source.getInt());
    this.amount = source.getInt();
  }

  public TransferTransaction(Integer from, Integer to, int amount) {
    this.from = from;
    this.to = to;
    this.amount = amount;
  }

  @Override
  public void executeOn(Map<Integer, PrevaylerAccount> accounts, Date time) {
    accounts.get(from).transfer(-amount);
    accounts.get(to).transfer(amount);
  }

  @Override
  public ByteBuffer stash(ByteBuffer target) {
    target.putInt(from);
    target.putInt(to);
    target.putInt(amount);
    return target;
  }
}
