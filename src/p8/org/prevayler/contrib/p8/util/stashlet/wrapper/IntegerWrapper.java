package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class IntegerWrapper extends Wrapper {

  public IntegerWrapper() {
    super(java.lang.Integer.class);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Integer.valueOf(s.getInt()));";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = Integer.valueOf({source}.getInt());";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putInt, {property});";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putInt({property});";
  }

}