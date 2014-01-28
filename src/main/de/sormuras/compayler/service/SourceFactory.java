package de.sormuras.compayler.service;

import java.util.List;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Unit;

public interface SourceFactory {

  Source createSource(Compayler compayler, List<Unit<?>> units);

}
