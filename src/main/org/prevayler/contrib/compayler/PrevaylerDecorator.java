package org.prevayler.contrib.compayler;

import org.prevayler.Prevayler;

/**
 * Decorator interface.
 * 
 * @author Christian Stein
 */
public interface PrevaylerDecorator<P> {

  Prevayler<P> prevayler();

}
