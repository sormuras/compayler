package de.sormuras.compayler.service;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.model.Description;

public interface DescriptionVisitor<X> {

  boolean visit(Compayler compayler, Description<X> description);

}
