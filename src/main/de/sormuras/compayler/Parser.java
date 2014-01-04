package de.sormuras.compayler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.sormuras.compayler.Compayler.CompaylerHint;
import de.sormuras.compayler.Compayler.ExecutionTime;
import de.sormuras.compayler.Descriptor.Field;

public class Parser implements Compayler.Parser {
  

  public static boolean isAnnotationPresent(Class<? extends Annotation> annotationClass, Annotation... annotations) {
    for (Annotation annotation : annotations)
      if (annotation.annotationType() == annotationClass)
        return true;
    return false;
  }

  public static boolean isExecutionTimePresent(Annotation... annotations) {
    return isAnnotationPresent(ExecutionTime.class, annotations);
  }

  private final Class<?> interfaceClass;

  public Parser(Class<?> interfaceClass) {
    this.interfaceClass = interfaceClass;
  }

  @Override
  public List<Descriptor> parse(String className) {
    List<Descriptor> descriptors = new ArrayList<>();

    for (Method method : interfaceClass.getMethods()) {
      Descriptor descriptor = new Descriptor(method.getName());
      descriptor.setClassName(method.getName().toUpperCase().charAt(0) + method.getName().substring(1));
      descriptor.setPackageName(interfaceClass.getPackage().getName());
      descriptor.setReturnType(method.getReturnType().getCanonicalName());
      for (Class<?> exceptionType : method.getExceptionTypes()) {
        descriptor.getThrowables().add(exceptionType.getCanonicalName());
      }
      if (method.isAnnotationPresent(CompaylerHint.class)) {
        descriptor.setMode(method.getAnnotation(CompaylerHint.class).value());
      }
      int lastIndex = method.getParameterTypes().length - 1;
      for (int index = 0; index <= lastIndex; index++) {
        Field field = descriptor.addField("p" + index);
        field.setIndex(index);
        field.setFirst(index == 0);
        field.setLast(index == lastIndex);
        field.setType(method.getParameterTypes()[index].getCanonicalName());
        field.setTime(isExecutionTimePresent(method.getParameterAnnotations()[index]));
        field.setVariable(field.isLast() && method.isVarArgs());
      }
      descriptors.add(descriptor);
    }

    return descriptors;
  }

}
