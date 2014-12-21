package org.prevayler.contrib.p8.util.stashlet.primitive;

public class LongPrimitive extends Primitive {

  public LongPrimitive() {
    super(long.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getLong();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putLong({property});";
  }

}