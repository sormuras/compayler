package org.prevayler.contrib.p8.benchmark;

import java.util.concurrent.Callable;

import org.prevayler.Prevayler;

public class Producer implements Callable<Producer> {

  private int operationCounter;
  private final Prevayler<StringBuilder> prevayler;

  public Producer(Prevayler<StringBuilder> prevayler) {
    this.prevayler = prevayler;
  }

  @Override
  public Producer call() throws Exception {
    for (int i = 0; i < 1000; i++) {
      prevayler.execute(Operation.A);
      operationCounter++;
    }
    return this;
  }
  
  public int getOperationCounter() {
    return operationCounter;
  }

}