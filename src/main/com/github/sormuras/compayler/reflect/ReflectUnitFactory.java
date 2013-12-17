package com.github.sormuras.compayler.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.sormuras.compayler.Unit;
import com.github.sormuras.compayler.UnitFactory;

public class ReflectUnitFactory implements UnitFactory {

  private final Class<?> interfaceClass;

  public ReflectUnitFactory(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  @Override
  public List<Unit> createUnits(String className) {
    if (!interfaceClass.getName().equals(className))
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    List<Unit> units = new ArrayList<>();
    for (Method method : interfaceClass.getMethods()) {
      Unit unit = new Unit(method.getName());
      unit.packageName = interfaceClass.getPackage().getName();
      unit.returnType = method.getReturnType().getCanonicalName();
      int index = 0;
      for (Class<?> parameter : method.getParameterTypes()) {
        unit.parameterNames.add("p" + index++);
        unit.parameterTypes.add(parameter.getCanonicalName());
      }
      for (Class<?> exception : method.getExceptionTypes()) {
        unit.throwing.add(exception.getCanonicalName());
      }
      units.add(unit);
    }

    return units;
  }

}
