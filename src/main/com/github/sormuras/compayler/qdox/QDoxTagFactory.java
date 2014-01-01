package com.github.sormuras.compayler.qdox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sormuras.compayler.PrevalentMethod;
import com.github.sormuras.compayler.PrevalentMode;
import com.github.sormuras.compayler.PrevalentType;
import com.github.sormuras.compayler.Tag;
import com.github.sormuras.compayler.TagFactory;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaType;

public class QDoxTagFactory implements TagFactory {

  private final JavaProjectBuilder builder;

  public QDoxTagFactory(String... sourceFiles) {
    this.builder = new JavaProjectBuilder();
    try {
      for (String sourceFile : sourceFiles)
        builder.addSource(new File(sourceFile));
    } catch (IOException e) {
      throw new RuntimeException("Adding source to QDox project builder failed!", e);
    }
  }

  @Override
  public Map<String, Boolean> createUniques(String className) {
    JavaClass javaClass = builder.getClassByName(className);
    if (javaClass == null)
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    Map<String, Boolean> uniques = new HashMap<>();
    for (JavaMethod method : javaClass.getMethods()) {
      String name = method.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old != null)
        uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  @Override
  public List<Tag> createTags(String className) {
    JavaClass javaClass = builder.getClassByName(className);
    if (javaClass == null)
      throw new IllegalStateException("Couldn't retrieve class for name: " + className);

    Map<String, Boolean> uniques = createUniques(className);

    List<Tag> tags = new ArrayList<>();
    for (JavaMethod method : javaClass.getMethods(true)) {
      String name = method.getName();
      String packageName = javaClass.getSource().getPackageName();
      String returnType = method.getReturnType(true).getGenericFullyQualifiedName();
      List<String> names = new ArrayList<>();
      List<String> types = new ArrayList<>();
      List<String> thros = new ArrayList<>();
      for (JavaParameter parameter : method.getParameters()) {
        names.add(parameter.getName());
        if (parameter.isVarArgs()) {
          types.add(parameter.getType().getGenericFullyQualifiedName() + "...");
        } else {
          types.add(parameter.getType().getGenericFullyQualifiedName());
        }
      }
      for (JavaType exception : method.getExceptionTypes()) {
        thros.add(exception.getGenericFullyQualifiedName());
      }
      Tag tag = new Tag(name, packageName, names, types, returnType, thros, uniques.get(name));
      for (JavaAnnotation annotation : method.getAnnotations()) {
        if (!PrevalentMethod.class.getName().equals(annotation.getType().getFullyQualifiedName()))
          continue;
        Object object = annotation.getNamedParameter("mode");
        if (object != null)
          tag.setPrevalentMode(PrevalentMode.valueOf(object.toString()));
        object = annotation.getNamedParameter("time");
        if (object != null)
          tag.setPrevalentTime(Integer.valueOf(object.toString()));
        object = annotation.getNamedParameter("value");
        if (object != null)
          tag.setPrevalentType(PrevalentType.valueOf(object.toString().substring(object.toString().lastIndexOf('.') + 1)));
      }
      tags.add(tag);
    }

    return tags;
  }
}
