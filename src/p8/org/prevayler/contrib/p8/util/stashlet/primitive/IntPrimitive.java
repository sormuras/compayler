package org.prevayler.contrib.p8.util.stashlet.primitive;

public class IntPrimitive extends Primitive {

  public IntPrimitive() {
    super(int.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getInt();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putInt({property});";
  }

}