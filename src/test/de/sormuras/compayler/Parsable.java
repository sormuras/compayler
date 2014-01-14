package de.sormuras.compayler;

/**
 * @compayler queryNames = "^get.*|^is.*" directNames = "set.*"
 */
public interface Parsable {
  
  int getData();
  
  boolean isDataSet();
  
  void setData(int data);

}
