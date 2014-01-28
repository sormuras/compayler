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

}
