package de.sormuras.compayler.service.impl;

import java.lang.reflect.Method;

import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.service.DescriptionVisitor;

public class MethodDescriptionVisitor implements DescriptionVisitor<Method> {

  @Override
  public boolean visit(Description<Method> description) {
    // Method method = description.getSignature().getTag();
    // TODO if (method.isAnnotationPresent(CompaylerDirective.class))
    // TODO description.getVariable().setMode(method.getAnnotation(CompaylerDirective.class).value());
    return true;
  }

}
