package com.github.sormuras.compayler.qdox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.sormuras.compayler.Unit;
import com.github.sormuras.compayler.UnitFactory;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaType;

public class QDoxUnitFactory implements UnitFactory {

  private final JavaProjectBuilder builder;

  public QDoxUnitFactory(String sourceFile) {
    this.builder = new JavaProjectBuilder();
    try {
      builder.addSource(new File(sourceFile));
    } catch (IOException e) {
      throw new RuntimeException("Adding source to QDox project builder failed!", e);
    }
  }

  @Override
  public List<Unit> createUnits(String className) {
    JavaClass javaClass = builder.getClassByName(className);
    if (javaClass == null)
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    List<Unit> units = new ArrayList<>();
    for (JavaMethod method : javaClass.getMethods(true)) {
      Unit unit = new Unit(method.getName());
      unit.packageName = javaClass.getSource().getPackageName();
      unit.returnType = method.getReturnType(true).getGenericFullyQualifiedName();
      for (JavaParameter parameter : method.getParameters()) {
        unit.parameterNames.add(parameter.getName());
        unit.parameterTypes.add(parameter.getType().getGenericFullyQualifiedName());
      }
      for (JavaType exception : method.getExceptionTypes()) {
        unit.throwing.add(exception.getGenericFullyQualifiedName());
      }
      units.add(unit);
    }

    return units;
  }

}
