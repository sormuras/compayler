package org.prevayler.contrib.compayler;

import java.util.List;
import java.util.Map;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;
import org.prevayler.contrib.compayler.Compayler.ExecutionMode;

@Decorate
public interface Simplicissimus extends Runnable, Appendable, Comparable<Integer> {

  @Override
  int compareTo(Integer other);

  @Execute(ExecutionMode.QUERY)
  Map<String, List<String>> getMap(Map<String, List<String>> map);

}
