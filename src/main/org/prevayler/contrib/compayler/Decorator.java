package org.prevayler.contrib.compayler;

import java.io.IOException;

import org.prevayler.Prevayler;

/**
 * Decorator base class.
 * 
 * @author Christian Stein
 */
public abstract class Decorator<PI, P extends PI> implements AutoCloseable {

  protected final Prevayler<P> prevayler;
  protected final P prevalentSystem;

  protected Decorator(Prevayler<P> prevayler) {
    this.prevayler = prevayler;
    this.prevalentSystem = prevayler.prevalentSystem();
  }

  @SuppressWarnings("unchecked")
  public PI asPrevalentInterface() {
    return (PI) this;
  }

  @Override
  public void close() throws IOException {
    prevayler().close();
  }

  public Prevayler<P> prevayler() {
    return prevayler;
  }

  protected PI redirect(PI result) {
    return result == prevalentSystem ? asPrevalentInterface() : result;
  }

}
