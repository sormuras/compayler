package org.prevayler.contrib.p8.util.stashlet.common;

public class StashableStashlet extends AbstractStashlet {

  public StashableStashlet(String typeName) {
    super(typeName);
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = new {type}({source});";
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, {type}::new);";
  }

  @Override
  public String getStashStraight() {
    return "{property}.stash({target});";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {property});";
  }

}