package com.github.sormuras.compayler;

/**
 * Executable class implementation feature descriptor bean unit.
 * 
 * @author Christian Stein
 */
public class Unit {

  private String className;
  private final Tag tag;

  public Unit(Tag tag) {
    this.tag = tag;
  }

  public String getClassName() {
    return className;
  }

  public Tag getTag() {
    return tag;
  }

  public void setClassName(String className) {
    this.className = className;
  }

}
