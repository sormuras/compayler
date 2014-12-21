package org.prevayler.contrib.p8.util.stashlet.primitive;

public class CharPrimitive extends Primitive {

  public CharPrimitive() {
    super(char.class);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.getChar();";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putChar({property});";
  }

}