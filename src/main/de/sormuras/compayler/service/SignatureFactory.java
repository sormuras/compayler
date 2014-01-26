package de.sormuras.compayler.service;

import java.util.List;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.model.Signature;

public interface SignatureFactory<X> {

  List<Signature<X>> createSignatures(Compayler compayler);

}
