package org.prevayler.contrib.compayler.service;

import java.util.LinkedList;
import java.util.List;

public class Listing {

  private final StringBuilder builder;
  private int depth = 0;
  private final String indentation = "  ";
  private final List<String> lines;

  public Listing() {
    this.builder = new StringBuilder();
    this.lines = new LinkedList<>();
  }

  public Listing add(String format, Object... args) {
    builder.setLength(0);
    for (int i = 0; i < depth; i++) {
      builder.append(indentation);
    }
    builder.append(args.length == 0 ? format : String.format(format, args));
    lines.add(builder.toString());
    return this;
  }

  public Listing clear() {
    builder.setLength(0);
    lines.clear();
    return this;
  }

  public Listing dec() {
    depth--;
    if (depth < 0)
      depth = 0;
    return this;
  }

  public Listing inc() {
    depth++;
    return this;
  }

  public List<String> list() {
    return lines;
  }

}
