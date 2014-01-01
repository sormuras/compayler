package com.github.sormuras.compayler;

/**
 * Executable class implementation feature descriptor bean unit.
 * 
 * @author Christian Stein
 */
public class Unit {

  private String className;
  private String packageName;
  private long serialVersionUID;
  private final Tag tag;

  public Unit(Tag tag) {
    this.tag = tag;
    setPackageName(tag.getPackageName());
    setSerialVersionUID(0L);
  }

  public String getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }
  
  public Tag getTag() {
    return tag;
  }
  
  public void setClassName(String className) {
    this.className = className;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

}
