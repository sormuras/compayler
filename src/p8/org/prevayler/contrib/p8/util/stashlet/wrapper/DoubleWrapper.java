package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class DoubleWrapper extends Wrapper {

  public DoubleWrapper() {
    super(java.lang.Double.class);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Double.valueOf(s.getDouble()));";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = Double.valueOf({source}.getDouble());";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putDouble, {property});";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putDouble({property});";
  }

}