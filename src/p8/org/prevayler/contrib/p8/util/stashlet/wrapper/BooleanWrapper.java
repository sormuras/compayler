package org.prevayler.contrib.p8.util.stashlet.wrapper;

public class BooleanWrapper extends Wrapper {

  public BooleanWrapper() {
    super(java.lang.Boolean.class);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> s.get() == 1 ? Boolean.TRUE : Boolean.FALSE);";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {source}.get() == 1 ? Boolean.TRUE : Boolean.FALSE;";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {property}, (t) -> t.put((byte) ({property} == Boolean.TRUE || {property} ? 1 : 0)));";
  }

  @Override
  public String getStashStraight() {
    return "{target}.put((byte) ({property} == Boolean.TRUE || {property} ? 1 : 0));";
  }

}