package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sormuras.compayler.Compayler.Configuration;
import com.github.sormuras.compayler.Compayler.DescriptionFactory;
import com.github.sormuras.compayler.Compayler.Directive;
import com.github.sormuras.compayler.Compayler.Mode;
import com.github.sormuras.compayler.Description.Field;
import com.github.sormuras.compayler.Description.Signature;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaGenericDeclaration;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.JavaTypeVariable;

public class Parser implements DescriptionFactory {

  private final Configuration configuration;
  private final JavaProjectBuilder javaProjectBuilder;

  public Parser(Configuration configuration) {
    this(configuration, new JavaProjectBuilder());
  }

  public Parser(Configuration configuration, JavaProjectBuilder javaProjectBuilder) {
    this.configuration = configuration;
    this.javaProjectBuilder = javaProjectBuilder;
  }

  protected Map<String, Boolean> buildNameIsUniqueMap(JavaClass javaClass) {
    Map<String, Boolean> uniques = new HashMap<>();
    for (JavaMethod method : javaClass.getMethods(true)) {
      String name = method.getName();
      Boolean old = uniques.put(name, Boolean.TRUE);
      if (old != null)
        uniques.put(name, Boolean.FALSE);
    }
    return uniques;
  }

  @Override
  public List<Description> createDescriptions() {
    JavaClass javaClass = javaProjectBuilder.getClassByName(configuration.getInterfaceName());
    if (javaClass == null)
      throw new IllegalStateException("Couldn't retrieve interface for name: " + configuration.getInterfaceName());

    for (JavaTypeVariable<JavaGenericDeclaration> typeVar : javaClass.getTypeParameters()) {
      configuration.getTypeParameters().add(typeVar.getName());
    }
    
    Matcher queryNameMatcher = Pattern.compile("123").matcher("");
    Matcher directNameMatcher = Pattern.compile("123").matcher("");

    DocletTag classCompaylerTag = javaClass.getTagByName("compayler");
    if (classCompaylerTag != null) {
      queryNameMatcher = Pattern.compile(classCompaylerTag.getNamedParameter("queryNames")).matcher("");
      directNameMatcher = Pattern.compile(classCompaylerTag.getNamedParameter("directNames")).matcher("");
    }

    Map<String, Boolean> uniques = buildNameIsUniqueMap(javaClass);
    List<Description> descriptions = new ArrayList<>();

    for (JavaMethod method : javaClass.getMethods(true)) {

      String name = method.getName();
      String returnType = method.getReturnType(true).getGenericFullyQualifiedName();
      List<String> throwables = new ArrayList<>();
      for (JavaType exception : method.getExceptionTypes()) {
        throwables.add(exception.getGenericFullyQualifiedName());
      }
      // parse parameters to fields
      List<Field> fields = new ArrayList<>();
      int index = 0;
      for (JavaParameter parameter : method.getParameters()) {
        Field field = new Field();
        field.setIndex(index++);
        field.setName(parameter.getName());
        field.setTime(!parameter.getAnnotations().isEmpty());
        field.setType(parameter.getType().getGenericFullyQualifiedName() + (parameter.isVarArgs() ? "[]" : ""));
        field.setVariable(parameter.isVarArgs());
        fields.add(field);
      }

      // create description
      Signature signature = new Signature(name, returnType, fields, throwables, uniques.get(name));
      Description description = new Description(configuration, signature);
      // update mode, if possible
      if (queryNameMatcher.reset(name).matches()) {
        description.getVariable().setMode(Mode.QUERY);
      }
      if (directNameMatcher.reset(name).matches()) {
        description.getVariable().setMode(Mode.DIRECT);
      }
      DocletTag compaylerTag = method.getTagByName("compayler");
      if (compaylerTag != null) {
        String mode = compaylerTag.getNamedParameter("mode");
        if (mode != null)
          description.getVariable().setMode(Mode.valueOf(mode.toUpperCase()));
      }
      for (JavaAnnotation annotation : method.getAnnotations()) {
        if (!Directive.class.getName().equals(annotation.getType().getFullyQualifiedName()))
          continue;
        Object object = annotation.getNamedParameter("value");
        if (object != null)
          description.getVariable().setMode(Mode.valueOf(object.toString().substring(object.toString().lastIndexOf('.') + 1)));
      }
      if (!method.getTypeParameters().isEmpty()) {
        for (JavaTypeVariable<JavaGenericDeclaration> parameter : method.getTypeParameters()) {
          description.getVariable().getTypeParameters().add(parameter.getName());
        }
      }
      // done
      descriptions.add(description);
    }

    return descriptions;
  }

  public JavaProjectBuilder getJavaProjectBuilder() {
    return javaProjectBuilder;
  }

}
