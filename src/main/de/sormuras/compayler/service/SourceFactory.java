package de.sormuras.compayler.service;

import java.util.List;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Description;

public interface SourceFactory {

  <X> Source createSource(Compayler compayler, List<Description<X>> descriptions);

}
