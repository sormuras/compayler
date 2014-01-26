package de.sormuras.compayler.service.impl;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.model.Signature;
import de.sormuras.compayler.service.SignatureFactory;

public class DefaultSignatureFactory implements SignatureFactory<Method> {

  @Override
  public List<Signature<Method>> createSignatures(Compayler compayler) {
    List<Signature<Method>> signatures = new LinkedList<>();
    return signatures;
  }

}
