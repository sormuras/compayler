package org.prevayler.contrib.p8.util.stashlet.primitive;

public class DoublePrimitive extends Primitive {

  public DoublePrimitive() {
    super(double.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getDouble();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putDouble({property});";
  }

}