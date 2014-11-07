package org.prevayler.contrib.p8.ported;

import org.prevayler.Transaction;

import java.util.Date;


class Appendix implements Transaction<AppendingSystem> {

  private static final long serialVersionUID = 7925676108189989759L;
  private final String appendix;

  public void executeOn(AppendingSystem prevalentSystem, Date ignored) {
    prevalentSystem.append(appendix);
  }

  Appendix(String appendix) {
    this.appendix = appendix;
  }
}