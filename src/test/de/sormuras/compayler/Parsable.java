package de.sormuras.compayler;

import java.util.Map;

/**
 * @compayler queryNames = "^get.*|^is.*" directNames = "set.*"
 */
public interface Parsable<E, T extends E> {

  <R> void addEntry(E entry, R example);

  Map<E, T> buildEntryMap(E[] entries, T[] types);

  int getData();

  E[] getEntries();

  boolean isDataSet();
  
  void setData(int data);

}
