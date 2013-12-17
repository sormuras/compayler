package com.github.sormuras.compayler;

import java.util.List;

public interface UnitFactory {

  List<Unit> createUnits(String className);

}
