package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class FloatWrapper extends Wrapper {

  public FloatWrapper() {
    super(java.lang.Float.class);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Float.valueOf(s.getFloat()));";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = Float.valueOf({source}.getFloat());";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putFloat, {property});";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putFloat({property});";
  }

}