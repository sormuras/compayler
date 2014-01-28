package de.sormuras.compayler.service;

import de.sormuras.compayler.model.Description;

public interface DescriptionVisitor<X> {

  boolean visit(Description<X> description);

}
