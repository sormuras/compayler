package de.sormuras.compayler.model;

import java.util.ArrayList;
import java.util.List;

import de.sormuras.compayler.Compayler;

/**
 * Holds the variable properties and the underlying signature.
 */
public abstract class Description<X> {

  private final Compayler compayler;
  private long serialVersionUID;
  private final Signature<X> signature;
  private List<Type> typeParameters;

  public Description(Compayler compayler, Signature<X> signature) {
    this.compayler = compayler;
    this.signature = signature;
    setSerialVersionUID(0L);
    setTypeParameters(new ArrayList<Type>());
  }

  public Compayler getCompayler() {
    return compayler;
  }

  public long getSerialVersionUID() {
    return serialVersionUID;
  }

  public Signature<X> getSignature() {
    return signature;
  }

  public List<Type> getTypeParameters() {
    return typeParameters;
  }

  public void setSerialVersionUID(long serialVersionUID) {
    this.serialVersionUID = serialVersionUID;
  }

  public void setTypeParameters(List<Type> typeParameters) {
    this.typeParameters = typeParameters;
  }

}
