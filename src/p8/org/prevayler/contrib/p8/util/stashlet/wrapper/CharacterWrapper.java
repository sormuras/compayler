package org.prevayler.contrib.p8.util.stashlet.wrapper;


public class CharacterWrapper extends Wrapper {
  
  public CharacterWrapper() {
    super(java.lang.Character.class);
  }
  
  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> Character.valueOf(s.getChar()));";
  }
  
  @Override
  public String getSpawnStraight() {
    return "{property} = Character.valueOf({source}.getChar());";
  }
  
  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putChar, {property});";
  }
  
  @Override
  public String getStashStraight() {
    return "{target}.putChar({property});";
  }
  
}