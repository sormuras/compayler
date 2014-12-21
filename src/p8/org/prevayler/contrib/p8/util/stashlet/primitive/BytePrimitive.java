package org.prevayler.contrib.p8.util.stashlet.primitive;

public class BytePrimitive extends Primitive {

  public BytePrimitive() {
    super(byte.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.get();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.put({property});";
  }

}