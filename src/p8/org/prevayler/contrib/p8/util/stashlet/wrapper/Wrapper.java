package org.prevayler.contrib.p8.util.stashlet.wrapper;

import org.prevayler.contrib.p8.util.stashlet.Stashlet;

public abstract class Wrapper implements Stashlet {

  private final String typeName;

  public Wrapper(Class<?> type) {
    if (!(Number.class.isAssignableFrom(type) || type.equals(Boolean.class) || type.equals(Character.class)))
      throw new IllegalArgumentException(type + " is not a legal wrapper for a primitive data type!");
    this.typeName = type.getTypeName();
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

}
