package com.github.sormuras.compayler;

import java.util.List;

public class DefaultUnitFactory implements UnitFactory {

  public String buildHashOfTypeNames(List<String> names) {
    long hash = 0L;
    for (String name : names) {
      // TODO remove generics? if (name.indexOf('<') > 0) name = name.substring(0, name.indexOf('<'));
      hash = 37 * hash + name.hashCode();
    }
    hash = Math.abs(hash);
    return Long.toString(hash, Character.MAX_RADIX).toUpperCase();
  }

  /**
   * Simple class name based on the method name and parameter types.
   * 
   * Examples:
   * <ul>
   * <li> {@code Run} for {@code java.lang.Runnable.run()}
   * <li> {@code Append$1TF86} for {@code java.lang.Appendable.append(char)}
   * <li> {@code Append$HKFHPX} for {@code java.lang.Appendable.append(java.lang.CharSequence)}
   * </ul>
   * 
   * @return simple class name based on the method name and parameter types
   */
  public String buildClassName(Unit unit) {
    Tag tag = unit.getTag();
    // capitalize unit name
    String name = tag.getName().toUpperCase().charAt(0) + tag.getName().substring(1);
    // append Query or Transaction
    PrevalentType type = tag.getPrevalentType();
    name = name + type.name().charAt(0) + type.name().substring(1).toLowerCase(); // Query | Transaction
    // done, if name is unique
    if (tag.isUnique()) {
      return name;
    }
    // name's not unique, append hash for parameter type name strings
    return name + "_" + buildHashOfTypeNames(tag.getParameterTypes());
  }

  @Override
  public Unit createUnit(Tag tag) {
    Unit unit = new Unit(tag);
    unit.setClassName(buildClassName(unit));
    return unit;
  }

}
