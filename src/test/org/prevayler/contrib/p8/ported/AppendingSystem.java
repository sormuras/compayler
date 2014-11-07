package org.prevayler.contrib.p8.ported;

import java.io.Serializable;


class AppendingSystem implements Serializable {

  private static final long serialVersionUID = -1151588644550257284L;
  private String value = "";

  String value() {
    return value;
  }

  void append(String appendix) {
    value = value + appendix;
    if (appendix.equals("rollback")) throw new RuntimeException("Testing Rollback");
  }

}