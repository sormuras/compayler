package de.sormuras.compayler.model;

import java.util.zip.CRC32;

import de.sormuras.compayler.Compayler;

public class Unit<X> extends Description<X> {

  public Unit(Compayler compayler, Signature<X> signature) {
    super(compayler, signature);
  }

  public String generateChecksum() {
    CRC32 crc32 = new CRC32();
    crc32.reset();
    for (Field field : getSignature().getFields()) {
      crc32.update(field.getType().getName().getBytes());
    }
    return Long.toString(crc32.getValue(), Character.MAX_RADIX).toUpperCase();
  }

  public String generateClassName() {
    StringBuilder builder = new StringBuilder();
    builder.append(getSignature().getName().toUpperCase().charAt(0));
    builder.append(getSignature().getName().substring(1));
    if (!getSignature().isUnique())
      builder.append(generateChecksum());
    return builder.toString();
  }

  public String generateClassNameWithTypeVariables() {
    StringBuilder typeVarBuilder = new StringBuilder();
    typeVarBuilder.append(generateClassName());
    // if (!getCompayler().getTypeParameters().isEmpty() || !getTypeParameters().isEmpty()) {
    // TODO typeVarBuilder.append(getCompayler().getTypeParameterParenthesis(getTypeParameters()));
    // }
    return typeVarBuilder.toString();
  }

  public String generateImplements() {
    StringBuilder builder = new StringBuilder();
    Kind kind = generateKind();
    builder.append(kind.getExecutableType());
    builder.append('<');
    builder.append(getCompayler().getInterfaceClassName());
    // builder.append(getCompayler().getTypeParameterParenthesis()); TODO Introduce InterfaceType!
    if (kind != Kind.TRANSACTION) {
      builder.append(',').append(' ').append(getSignature().getReturnType().getWrapped());
    }
    builder.append('>');
    return builder.toString();
  }

  public Kind generateKind() {
    if (getMode() == Mode.QUERY)
      return Kind.QUERY;
    if (getSignature().getReturnType().isVoid())
      return Kind.TRANSACTION;
    if (getSignature().getThrowables().isEmpty())
      return Kind.TRANSACTION_QUERY;
    // if nothing applies...
    return Kind.TRANSACTION_QUERY_EXCEPTION;
  }

  public String generateParameterSignature() {
    StringBuilder builder = new StringBuilder();
    builder.append('(');
    for (Field field : getSignature().getFields()) {
      if (field.getIndex() > 0)
        builder.append(", ");
      builder.append(field.getType().toString(field.isVariable()));
      builder.append(' ').append(field.getName());
    }
    builder.append(')');
    return builder.toString();
  }

}
