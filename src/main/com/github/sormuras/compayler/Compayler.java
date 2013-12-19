package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.List;

public class Compayler {

  private final UnitFactory tagFactory;
  private final TagFactory unitFactory;

  public Compayler(UnitFactory tagFactory, TagFactory unitFactory) {
    this.tagFactory = tagFactory;
    this.unitFactory = unitFactory;
  }

  /**
   * @return list of tags as created by the configured tag factory
   */
  public List<Tag> createTags(String className) {
    List<Tag> units = unitFactory.createTags(className);
    return units;
  }

  /**
   * @return list of units as created by the configured unit factory
   */
  public List<Unit> createUnits(List<Tag> units) {
    List<Unit> tags = new ArrayList<>(units.size());
    for (Tag unit : units) {
      tags.add(tagFactory.createUnit(unit));
    }
    return tags;
  }

}
