package org.prevayler.contrib.compayler.prevayler;

import org.prevayler.Prevayler;

public class VolatilePrevaylerFactory<P> implements PrevaylerFactory<P> {

  @Override
  public Prevayler<P> createPrevayler(P prevalentSystem, ClassLoader loader) {
    return new VolatilePrevayler<>(prevalentSystem, loader);
  }

}