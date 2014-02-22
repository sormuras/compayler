package org.prevayler.contrib.compayler;

import java.util.List;

/**
 * Read-only description of an executable action.
 * 
 * @author Christian Stein
 */
public interface Unit {

  Mode getMode();

  String getName();

  List<Param> getParams();

  Type getReturnType();

  Long getSerialVersionUID();

  List<Type> getThrowables();

  boolean isUnique();

}
