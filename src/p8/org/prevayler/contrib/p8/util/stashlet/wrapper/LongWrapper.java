package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class LongWrapper extends Wrapper {
  
  public LongWrapper() {
    super(java.lang.Long.class);
  }
  
  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Long.valueOf(s.getLong()));";
  }
  
  @Override
  public String getSpawnStraight() {
    return "{property} = Long.valueOf({source}.getLong());";
  }
  
  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putLong, {property});";
  }
  
  @Override
  public String getStashStraight() {
    return "{target}.putLong({property});";
  }
  
}