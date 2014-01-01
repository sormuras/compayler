package com.github.sormuras.compayler;

import java.util.List;

public class DefaultUnitFactory implements UnitFactory {

  // TODO move to utility class
  public static String wrap(String className) {
    switch (className) {
    case "boolean":
      return "java.lang.Boolean";
    case "byte":
      return "java.lang.Byte";
    case "char":
      return "java.lang.Character";
    case "double":
      return "java.lang.Double";
    case "float":
      return "java.lang.Float";
    case "int":
      return "java.lang.Integer";
    case "long":
      return "java.lang.Long";
    case "short":
      return "java.lang.Short";
    case "void":
      return "java.lang.Void";
    default:
      return className;
    }
  }

  /**
   * Simple class name based on the method name and parameter types.
   * 
   * Examples:
   * <ul>
   * <li> {@code Run} for {@code java.lang.Runnable.run()}
   * <li> {@code Append_1TF86} for {@code java.lang.Appendable.append(char)}
   * <li> {@code Append_HKFHPX} for {@code java.lang.Appendable.append(java.lang.CharSequence)}
   * </ul>
   * 
   * @return simple class name based on the method name and parameter types
   */
  public String buildClassName(Tag tag) {
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

  public String buildHashOfTypeNames(List<String> names) {
    long hash = 0L;
    for (String name : names) {
      // TODO remove generics? if (name.indexOf('<') > 0) name = name.substring(0, name.indexOf('<'));
      hash = 37 * hash + name.hashCode();
    }
    hash = Math.abs(hash);
    return Long.toString(hash, Character.MAX_RADIX).toUpperCase();
  }

  @Override
  public Unit createUnit(Tag tag) {
    Unit unit = new Unit(tag);
    unit.setClassName(buildClassName(tag));
    unit.setReturnType(wrap(tag.getReturnType()));
    return unit;
  }

}
