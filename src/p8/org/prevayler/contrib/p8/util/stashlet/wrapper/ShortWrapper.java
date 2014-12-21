package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class ShortWrapper extends Wrapper {

  public ShortWrapper() {
    super(java.lang.Short.class);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Short.valueOf(s.getShort()));";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = Short.valueOf({source}.getShort());";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putShort, {property});";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putShort({property});";
  }

}