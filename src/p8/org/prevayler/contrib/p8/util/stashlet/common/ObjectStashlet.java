package org.prevayler.contrib.p8.util.stashlet.common;

public class ObjectStashlet extends AbstractStashlet {

  public ObjectStashlet(String typeName) {
    super(typeName);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, {type}.class);";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {property});";
  }

}