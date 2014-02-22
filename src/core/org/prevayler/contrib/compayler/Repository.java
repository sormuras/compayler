package org.prevayler.contrib.compayler;

import java.util.List;

/**
 * Bundles credentials needed for decorator source code generation.
 * 
 * @author Christian Stein
 */
public interface Repository {

  Type getDecoratorType();

  Type getInterfaceType();

  Type getSuperType();

  List<Unit> getUnits();

}
