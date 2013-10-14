package org.prevayler.contrib.compayler;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;

/**
 * Prevayler factory interface.
 * 
 * @author Christian Stein
 */
public interface PrevaylerCreator<PI> {

  class DefaultPrevaylerCreator<PI> implements PrevaylerCreator<PI> {

    private final String prevalenceBase;
    private final PI prevalentSystem;

    public DefaultPrevaylerCreator(PI prevalentSystem) {
      this(prevalentSystem, null);
    }

    public DefaultPrevaylerCreator(PI prevalentSystem, String prevalenceBase) {
      this.prevalentSystem = prevalentSystem;
      this.prevalenceBase = prevalenceBase;
    }

    @Override
    public Prevayler<PI> createPrevayler(ClassLoader loader) throws Exception {
      Thread.currentThread().setContextClassLoader(loader);
      return PrevaylerFactory.createPrevayler(prevalentSystem, prevalenceBase);
    }

  }

  class TransientPrevaylerCreator<PI> implements PrevaylerCreator<PI> {

    private final PI prevalentSystem;

    public TransientPrevaylerCreator(PI prevalentSystem) {
      this.prevalentSystem = prevalentSystem;
    }

    @Override
    public Prevayler<PI> createPrevayler(ClassLoader loader) throws Exception {
      Thread.currentThread().setContextClassLoader(loader);
      return PrevaylerFactory.createTransientPrevayler(prevalentSystem);
    }

  }

  Prevayler<PI> createPrevayler(ClassLoader loader) throws Exception;

}
