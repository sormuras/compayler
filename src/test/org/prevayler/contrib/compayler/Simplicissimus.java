package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.DIRECT;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.QUERY;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;
import org.prevayler.contrib.compayler.Compayler.ExecutionTime;

@Decorate
public interface Simplicissimus extends Runnable, Appendable, Comparable<Integer> {

  Appendable append(CharSequence csq, int start, int end) throws IOException;

  Simplicissimus chain(@ExecutionTime Date time) throws TimeoutException;

  @Override
  int compareTo(Integer other);

  @Execute(QUERY)
  Map<String, List<String>> generateMap(Map<String, List<String>> map, String... more);

  List<Integer> getNumbers(); // Query by name

  @Override
  @Execute(DIRECT)
  void run();

}
