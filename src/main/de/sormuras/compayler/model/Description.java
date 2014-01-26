package de.sormuras.compayler.model;

import de.sormuras.compayler.Compayler;

public class Description<X> {

  private final Compayler compayler;
  private final Signature<X> signature;

  public Description(Compayler compayler, Signature<X> signature) {
    this.compayler = compayler;
    this.signature = signature;
  }

  public Compayler getCompayler() {
    return compayler;
  }

  public Signature<X> getSignature() {
    return signature;
  }

}
