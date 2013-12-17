package com.github.sormuras.compayler;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Some method providing interface for testing.
 */
public interface Api {

  List<Map<String, Number>> listOfMaps(Date date, byte flag, List<TimeUnit> times) throws NumberFormatException,
      ConcurrentModificationException;

}
