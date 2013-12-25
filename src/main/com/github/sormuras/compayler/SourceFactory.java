package com.github.sormuras.compayler;

import java.util.List;

public interface SourceFactory {

  Source createDecoratorSource(List<Unit> units);

  Source createExecutableSource(Unit unit);

}
