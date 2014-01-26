package de.sormuras.compayler.service.impl;

import java.util.List;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.service.SourceFactory;

public class DefaultSourceFactory implements SourceFactory {

  @Override
  public <X> Source createSource(Compayler compayler, List<Description<X>> descriptions) {
    Lines lines = new Lines();
    lines.add("/**", " * Generated for " + compayler, " */");
    return new Source(compayler.getDecoratorPackage(), compayler.getDecoratorName(), lines.getLines());
  }

}
