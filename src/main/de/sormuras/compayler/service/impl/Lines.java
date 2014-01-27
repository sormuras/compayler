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

  public Lines add(CharSequence... csqs) {
    for (CharSequence csq : csqs) {
      builder.setLength(0);
      if (csq.length() > 0)
        for (int i = 0; i < depth; i++) {
          builder.append(indentation);
        }
      builder.append(csq);
      lines.add(builder.toString());
    }
    return this;
  }

  public Lines clear() {
    builder.setLength(0);
    lines.clear();
    return this;
  }

  public List<String> getLines() {
    return lines;
  }

  public Lines popIndention() {
    depth--;
    if (depth < 0)
      depth = 0;
    return this;
  }

  public Lines pushIndention() {
    depth++;
    return this;
  }

}
