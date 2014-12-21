package org.prevayler.contrib.p8.util.stashlet.wrapper;

public class ByteWrapper extends Wrapper {
  
  public ByteWrapper() {
    super(java.lang.Byte.class);
  }
  
  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, {source}::get);";
  }
  
  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.get();";
  }
  
  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::put, {property});";
  }
  
  @Override
  public String getStashStraight() {
    return "{target}.put({property});";
  }
  
}