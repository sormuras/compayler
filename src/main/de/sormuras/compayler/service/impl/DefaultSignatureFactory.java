package de.sormuras.compayler.service.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.ExecutionTime;
import de.sormuras.compayler.model.Field;
import de.sormuras.compayler.model.Signature;
import de.sormuras.compayler.model.Type;
import de.sormuras.compayler.service.SignatureFactory;

public class DefaultSignatureFactory implements SignatureFactory<Method> {

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

  @Override
  public List<Signature<Method>> createSignatures(Compayler compayler) {
    List<Signature<Method>> signatures = new LinkedList<>();

    Class<?> interfaceClass = compayler.getInterfaceClass();
    if (interfaceClass == null)
      throw new RuntimeException("Can't create signatures for " + compayler.getInterfaceClassName());

    Map<String, Boolean> uniques = buildNameIsUniqueMap(interfaceClass);
    for (Method method : interfaceClass.getMethods()) {
      // simple strings
      String name = method.getName();
      Type returnType = Type.forClass(method.getReturnType());
      // collect exception type names
      List<Type> throwables = new ArrayList<>();
      for (Class<?> exceptionType : method.getExceptionTypes())
        throwables.add(Type.forClass(exceptionType));
      // parse parameters to fields
      List<Field> fields = new ArrayList<>();
      int lastIndex = method.getParameterTypes().length - 1;
      for (int index = 0; index <= lastIndex; index++) {
        Field field = new Field();
        field.setIndex(index);
        field.setName("p" + index);
        field.setTime(isExecutionTimePresent(method.getParameterAnnotations()[index]));
        field.setType(Type.forClass(method.getParameterTypes()[index]));
        field.setVariable(index == lastIndex && method.isVarArgs());
        fields.add(field);
      }
      // create signature
      Signature<Method> signature = new Signature<>(method, name, returnType, fields, throwables, uniques.get(name));
      signatures.add(signature);
    }

    return signatures;
  }

}
