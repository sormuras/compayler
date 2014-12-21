package org.prevayler.contrib.p8.util.stashlet.primitive;

public class FloatPrimitive extends Primitive {

  public FloatPrimitive() {
    super(float.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getFloat();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putFloat({property});";
  }

}