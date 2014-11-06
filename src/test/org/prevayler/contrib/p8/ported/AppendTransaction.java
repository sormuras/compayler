package org.prevayler.contrib.p8.ported;

import org.prevayler.Transaction;

import java.util.Date;

public class AppendTransaction implements Transaction<StringBuffer> {

  private static final long serialVersionUID = -3830205386199825379L;
  public String toAdd;

  AppendTransaction() {
    // Skaringa requires a default constructor, but XStream does not.
  }

  public AppendTransaction(String toAdd) {
    this.toAdd = toAdd;
  }

  public void executeOn(StringBuffer prevalentSystem, Date executionTime) {
    prevalentSystem.append(toAdd);
  }

}
