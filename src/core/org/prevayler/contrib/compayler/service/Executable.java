package org.prevayler.contrib.compayler.service;

import org.prevayler.contrib.compayler.Mode;
import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Unit;

/**
 * Prevayler action/executable interface information.
 * 
 * @author Christian Stein
 */
public enum Executable {

  /**
   * Represents an atomic query that can be executed on a Prevalent System that returns a result or throws an Exception after executing.
   */
  QUERY("Query", "%s query", true),

  /**
   * An atomic transaction to be executed on a Prevalent System.
   */
  TRANSACTION("Transaction", "%s executeOn", false),

  /**
   * An atomic transaction that also returns a result.
   */
  TRANSACTION_QUERY("SureTransactionWithQuery", "%s executeAndQuery", false),

  /**
   * An atomic transaction that also returns a result or throws an Exception after executing.
   */
  TRANSACTION_QUERY_EXCEPTION("TransactionWithQuery", "%s executeAndQuery", true);

  public static Executable forUnit(Unit unit) {
    if (unit.getMode() == Mode.QUERY)
      return QUERY;
    if (unit.getReturnType().isVoid())
      return TRANSACTION;
    if (unit.getThrowables().isEmpty())
      return TRANSACTION_QUERY;
    // if nothing applies...
    return TRANSACTION_QUERY_EXCEPTION;
  }

  private final String methodFormat;
  private final boolean throwingException;
  private final Type type;

  private Executable(String executableTypeName, String methodFormat, boolean throwingException) {
    this.type = new Type("org.prevayler." + executableTypeName);
    this.throwingException = throwingException;
    String signature = "(%s prevalentSystem, java.util.Date executionTime)" + (throwingException ? " throws java.lang.Exception" : "");
    this.methodFormat = methodFormat + signature;
  }

  public String getMethodFormat() {
    return methodFormat;
  }

  public Type getType() {
    return type;
  }

  public boolean isThrowingException() {
    return throwingException;
  }

  public String toString(Type returnType, Type prevalentSystem) {
    if (this == TRANSACTION)
      assert (returnType.getBinaryName().equals("void"));
    return String.format(getMethodFormat(), returnType.getCanonicalName(true), prevalentSystem);
  }

}
