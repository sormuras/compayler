package org.prevayler.contrib.p8.util.stashlet.primitive;

public class BooleanPrimitive extends Primitive {

  public BooleanPrimitive() {
    super(boolean.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.get() == 1;";
  }

  @Override
  public String getStashStraight() {
    return "{target}.put((byte) ({property} ? 1 : 0));";
  }

}