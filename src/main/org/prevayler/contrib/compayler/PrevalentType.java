package org.prevayler.contrib.compayler;

/**
 * Execution (read transaction) styles.
 * 
 * @author Christian Stein
 */
public enum PrevalentType {

  /**
   * Sensitive query that is not journaled.
   * 
   * @see org.prevayler.Query
   */
  QUERY,

  /**
   * Transation that is journaled for system recovery.
   * 
   * @see org.prevayler.Transaction
   * @see org.prevayler.TransactionWithQuery
   * @see org.prevayler.SureTransactionWithQuery
   */
  TRANSACTION

}