package de.sormuras.compayler.service.impl;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.service.DescriptionVisitor;

public class DefaultDescriptionVisitor<X> implements DescriptionVisitor<X> {

  @Override
  public boolean visit(Compayler compayler, Description<X> description) {
    return true;
  }

}
