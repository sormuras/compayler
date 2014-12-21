package org.prevayler.contrib.p8.util.stashlet.primitive;

import org.prevayler.contrib.p8.util.stashlet.Stashlet;

public abstract class Primitive implements Stashlet {
  
  private final String typeName;

  public Primitive(Class<?> type) {
    if (!type.isPrimitive())
      throw new IllegalArgumentException("Expected primitive type, but got: " + type);
    this.typeName = type.getTypeName();
  }

  @Override
  public final String getSpawnNullable() {
    throw new UnsupportedOperationException(getTypeName() + " can't be null!");
  }

  @Override
  public final String getStashNullable() {
    throw new UnsupportedOperationException(getTypeName() + " can't be null!");
  }
  
  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public final boolean isTypeNullable() {
    return false;
  }

}