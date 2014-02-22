package org.prevayler.contrib.compayler.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.prevayler.contrib.compayler.ExecutionTime;
import org.prevayler.contrib.compayler.Param;
import org.prevayler.contrib.compayler.Repository;
import org.prevayler.contrib.compayler.Shape;
import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Unit;
import org.prevayler.contrib.compayler.UnitFactory;

public class DefaultUnitFactory implements UnitFactory {

  public static boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, Annotation... annotations) {
    for (Annotation annotation : annotations)
      if (annotation.annotationType() == annotationClass)
        return true;
    return false;
  }

  public static boolean isExecutionTimePresent(Annotation... annotations) {
    return isAnnotationPresent(ExecutionTime.class, annotations);
  }

  public static Map<String, Boolean> buildNameIsUniqueMap(Class<?> interfaceClass) {
    Map<String, Boolean> uniques = new HashMap<>();
    for (Method method : interfaceClass.getMethods()) {
      String name = method.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old == null)
        continue;
      uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  protected Shape createShape(Method method, boolean unique) {
    List<Param> fields = new ArrayList<>();
    int lastIndex = method.getParameterTypes().length - 1;
    for (int index = 0; index <= lastIndex; index++) {
      Param field = new Param();
      field.setIndex(index);
      field.setName("p" + index);
      field.setTime(isExecutionTimePresent(method.getParameterAnnotations()[index]));
      field.setType(new Type(method.getParameterTypes()[index]));
      field.setVariable(index == lastIndex && method.isVarArgs());
      fields.add(field);
    }
    List<Type> throwables = new ArrayList<>();
    for (Class<?> exceptionType : method.getExceptionTypes())
      throwables.add(new Type(exceptionType));
    return new Shape(method.getName(), new Type(method.getReturnType()), fields, throwables, unique);
  }

  @Override
  public List<Unit> createUnits(Repository repository) {
    Type type = repository.getInterfaceType();
    if (type == null)
      throw new NullPointerException("Interface type must not be null!");

    Class<?> interfaceClass;
    try {
      interfaceClass = Class.forName(type.getBinaryName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class not loadable?!", e);
    }

    List<Unit> units = new LinkedList<>();
    Map<String, Boolean> uniques = buildNameIsUniqueMap(interfaceClass);
    for (Method method : interfaceClass.getMethods())
      units.add(createShape(method, uniques.get(method.getName())));

    return units;
  }

}
