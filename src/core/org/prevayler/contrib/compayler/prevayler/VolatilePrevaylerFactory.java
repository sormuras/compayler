package org.prevayler.contrib.compayler.prevayler;

import org.prevayler.Prevayler;

public class VolatilePrevaylerFactory<P> implements PrevaylerFactory<P> {

  private final P prevalentSystem;

  public VolatilePrevaylerFactory(P prevalentSystem) {
    this.prevalentSystem = prevalentSystem;
  }

  @Override
  public Prevayler<P> createPrevayler(ClassLoader loader) {
    return new VolatilePrevayler<>(prevalentSystem, loader);
  }

}