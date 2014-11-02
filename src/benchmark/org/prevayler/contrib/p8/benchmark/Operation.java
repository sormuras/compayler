package org.prevayler.contrib.p8.benchmark;

import java.util.Date;

import org.prevayler.Transaction;

public enum Operation implements Transaction<StringBuilder> {

  A, B;

  @Override
  public void executeOn(StringBuilder prevalentSystem, Date executionTime) {
    prevalentSystem.append(name());
  }

}