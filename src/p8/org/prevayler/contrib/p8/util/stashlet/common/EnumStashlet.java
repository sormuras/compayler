package org.prevayler.contrib.p8.util.stashlet.common;

public class EnumStashlet<E extends Enum<E>> extends AbstractStashlet {

  public EnumStashlet(Class<E> type) {
    super(type.getTypeName());
    if (!type.isEnum())
      throw new IllegalArgumentException(type + " is not an enum!");
  }

  public EnumStashlet(String typeName) {
    super(typeName);
  }

  @Override
  public String getSpawnNullable() {
    return "{property} = {spawn}({source}, (s) -> {type}.values()[s.getInt()]);";
  }

  @Override
  public String getSpawnStraight() {
    return "{property} = {type}.values()[{source}.getInt()];";
  }

  @Override
  public String getStashNullable() {
    return "{stash}({target}, {target}::putInt, {property}.ordinal());";
  }

  @Override
  public String getStashStraight() {
    return "{target}.putInt({property}.ordinal());";
  }

}