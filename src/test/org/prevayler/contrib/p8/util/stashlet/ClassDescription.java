package org.prevayler.contrib.p8.util.stashlet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ClassDescription {

  public static class Param {

    public final String name;
    public final boolean nullable;
    public final String type;

    public Param(String name, String type) {
      this(name, type, true);
    }

    public Param(String name, String type, boolean nullable) {
      this.name = name;
      this.type = type;
      this.nullable = nullable;
    }

  }

  public final String name;

  public final List<Param> params = new LinkedList<>();

  public ClassDescription(String name, Param... params) {
    this.name = name;
    this.params.addAll(Arrays.asList(params));
  }

}
