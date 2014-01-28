package de.sormuras.compayler.model;

/**
 * Execution mode.
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
   * Default mode.
   */
  TRANSACTION

}
