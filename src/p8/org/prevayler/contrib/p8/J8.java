package org.prevayler.contrib.p8;

import java.io.Closeable;
import java.io.Flushable;

public interface J8 extends Closeable, Flushable {

  void clear();

  int commit();

  <T> T copy(T object, long time);

  long getAge();

  double usage();

}
