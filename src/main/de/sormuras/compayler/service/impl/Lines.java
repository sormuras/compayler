package de.sormuras.compayler.service.impl;

import java.util.LinkedList;
import java.util.List;

public class Lines {

  private final StringBuilder builder;
  private int depth = 0;
  private String indentation = "  ";
  private final List<String> lines;

  public Lines() {
    this.builder = new StringBuilder();
    this.lines = new LinkedList<>();
  }

  public void add(CharSequence... csqs) {
    for (CharSequence csq : csqs) {
      resetBuilder();
      builder.append(csq);
      lines.add(builder.toString());
    }
  }

  public List<String> getLines() {
    return lines;
  }

  public void popIndention() {
    depth--;
    if (depth < 0)
      depth = 0;
  }

  public void pushIndention() {
    depth++;
  }

  private void resetBuilder() {
    builder.setLength(0);
    for (int i = 0; i < depth; i++) {
      builder.append(indentation);
    }
  }

}
