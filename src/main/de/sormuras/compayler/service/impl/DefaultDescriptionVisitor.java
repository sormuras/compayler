package de.sormuras.compayler.service.impl;

import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.service.DescriptionVisitor;

public class DefaultDescriptionVisitor<X> implements DescriptionVisitor<X> {

  @Override
  public boolean visit(Description<X> description) {
    return true;
  }

}
