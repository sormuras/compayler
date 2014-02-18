package org.prevayler.contrib.compayler;

/**
 * Execution mode.
 * 
 * @author Christian Stein
 */
public enum Mode {

  /**
   * By-pass decorator/prevayler and work directly on the underlying prevalent system.
   */
  DIRECT,

  /**
   * Query-only, which is not persistet by prevayler.
   */
  QUERY,

  /**
   * Transactions are journaled for system recovery.
   */
  TRANSACTION

}
