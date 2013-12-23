package com.github.sormuras.compayler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultTagFactory implements TagFactory {

  private final Class<?> interfaceClass;

  public DefaultTagFactory(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  @Override
  public Map<String, Boolean> createUniques(String className) {
    if (!interfaceClass.getName().equals(className))
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    Map<String, Boolean> uniques = new HashMap<>();
    for (Method method : interfaceClass.getMethods()) {
      String name = method.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old != null)
        uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  @Override
  public List<Tag> createTags(String className) {
    if (!interfaceClass.getName().equals(className))
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    Map<String, Boolean> uniques = createUniques(className);
    List<Tag> tags = new ArrayList<>();
    for (Method method : interfaceClass.getMethods()) {
      String name = method.getName();
      String packageName = interfaceClass.getPackage().getName();
      String returnType = method.getReturnType().getCanonicalName();
      List<String> names = new ArrayList<>();
      List<String> types = new ArrayList<>();
      List<String> thros = new ArrayList<>();
      int index = 0;
      for (Class<?> parameter : method.getParameterTypes()) {
        names.add("p" + index++);
        types.add(parameter.getCanonicalName());
      }
      for (Class<?> exception : method.getExceptionTypes()) {
        thros.add(exception.getCanonicalName());
      }
      Tag tag = new Tag(name, packageName, names, types, returnType, thros, uniques.get(name));
      PrevalentMethod anntotation = method.getAnnotation(PrevalentMethod.class);
      if (anntotation != null) {
        tag.setPrevalentMode(anntotation.mode());
        tag.setPrevalentTime(anntotation.time());
        tag.setPrevalentType(anntotation.value());
      }
      tags.add(tag);
    }

    return tags;
  }

}
