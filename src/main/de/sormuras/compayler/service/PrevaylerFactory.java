package de.sormuras.compayler.service;

import org.prevayler.Prevayler;

/**
 * Used to instantiate a prevayler implementation.
 */
public interface PrevaylerFactory<P> {

  /**
   * Creates default Prevayler.
   * 
   * @param loader
   *          The class loader that must be used to (de-)serialize transaction objects.
   * @return
   */
  Prevayler<P> createPrevayler(ClassLoader loader) throws Exception;

}
