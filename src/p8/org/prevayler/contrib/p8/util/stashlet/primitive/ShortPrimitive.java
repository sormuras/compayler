package org.prevayler.contrib.p8.util.stashlet.primitive;

public class ShortPrimitive extends Primitive {

  public ShortPrimitive() {
    super(short.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getShort();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putShort({property});";
  }

}