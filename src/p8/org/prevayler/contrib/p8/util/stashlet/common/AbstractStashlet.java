package org.prevayler.contrib.p8.util.stashlet.common;

import org.prevayler.contrib.p8.util.stashlet.Stashlet;

public abstract class AbstractStashlet implements Stashlet {

  private final String typeName;

  public AbstractStashlet(String typeName) {
    if (typeName == null || typeName.isEmpty())
      throw new IllegalArgumentException("Argument typeName must not be null nor empty!");
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }

}
