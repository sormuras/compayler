package com.github.sormuras.compayler;

/**
 * Executable class implementation feature descriptor bean unit.
 * 
 * @author Christian Stein
 */
public class Unit {

  private String className;
  private String packageName;
  private final Tag tag;

  public Unit(Tag tag) {
    this.tag = tag;
  }

  public String getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public Tag getTag() {
    return tag;
  }

  public void setClassName(String className) {
    this.className = className;
  }

}
