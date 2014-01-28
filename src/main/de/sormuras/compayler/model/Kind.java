package de.sormuras.compayler.model;

public enum Kind {

  QUERY("org.prevayler.Query", true),

  TRANSACTION("org.prevayler.Transaction", false),

  TRANSACTION_QUERY("org.prevayler.SureTransactionWithQuery", false),

  TRANSACTION_QUERY_EXCEPTION("org.prevayler.TransactionWithQuery", true);

  private final Type executableType;
  private final boolean throwingException;

  private Kind(String executableInterfaceName, boolean throwingException) {
    this.executableType = Type.forName(executableInterfaceName);
    this.throwingException = throwingException;
  }

  public Type getExecutableType() {
    return executableType;
  }

  public boolean isThrowingException() {
    return throwingException;
  }

}
