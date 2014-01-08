package com.github.sormuras.compayler;

import java.io.IOException;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import com.github.sormuras.compayler.Compayler.Directive;
import com.github.sormuras.compayler.Compayler.ExecutionTime;
import com.github.sormuras.compayler.Compayler.Mode;

public interface Testable<E> extends Deque<E> {

  @Directive(Mode.DIRECT)
  Testable<E> direct();

  long executionTime(@ExecutionTime Date time);

  Date executionTime(Date seed, @ExecutionTime Date time, Date... dates);

  /**
   * @compayler mode = QUERY
   */
  int query() throws Exception;

  Map<String, Date> zzz(List<Map<Integer, Number>> list, int... numbers) throws IllegalStateException, IOException;

}
