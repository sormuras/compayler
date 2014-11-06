package org.prevayler.contrib.p8.ported;

import org.prevayler.Query;

import java.util.Date;

public class NullQuery implements Query<Object, Object> {

  private static final long serialVersionUID = 1L;

  public Object query(Object prevalentSystem, Date executionTime) throws Exception {
    return null;
  }

}
