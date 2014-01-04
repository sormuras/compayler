package de.sormuras.compayler;

import java.io.IOException;

import org.prevayler.Prevayler;

/**
 * Decorator base class.
 * 
 * @author Christian Stein
 */
public abstract class Decorator<P> implements AutoCloseable {

  protected Prevayler<P> prevayler;
  protected P prevalentSystem;

  protected Decorator(Prevayler<P> prevayler) {
    this.prevayler = prevayler;
    this.prevalentSystem = prevayler.prevalentSystem();
  }

  @SuppressWarnings("unchecked")
  public P asPrevalentInterface() {
    return (P) this;
  }

  protected void checkNotClosed() throws IllegalStateException {
    if (prevayler == null)
      throw new IllegalStateException("closed");
  }

  @Override
  public void close() throws IOException {
    if (prevayler == null)
      return;
    prevayler().close();
    prevayler = null;
    prevalentSystem = null;
  }

  public Prevayler<P> prevayler() {
    checkNotClosed();
    return prevayler;
  }

  protected P redirect(P result) {
    checkNotClosed();
    return result == prevalentSystem ? asPrevalentInterface() : result;
  }

}
