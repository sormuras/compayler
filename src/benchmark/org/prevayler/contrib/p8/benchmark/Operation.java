package org.prevayler.contrib.p8.benchmark;

import java.nio.ByteBuffer;
import java.util.Date;

import org.prevayler.Transaction;
import org.prevayler.contrib.p8.util.Stashable;

public enum Operation implements Transaction<StringBuilder>, Stashable {

  A, B;

  @Override
  public void executeOn(StringBuilder prevalentSystem, Date executionTime) {
    prevalentSystem.append(name());
  }

  @Override
  public ByteBuffer stash(ByteBuffer target) {
    return target.put((byte) ordinal());
  }

}