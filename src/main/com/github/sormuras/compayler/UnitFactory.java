package com.github.sormuras.compayler;

import java.util.List;

public interface UnitFactory {

  String buildHashOfTypeNames(List<String> names);

  Unit createUnit(Tag tag);

}
