package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultSourceFactory implements SourceFactory {

  @Override
  public Source createDecoratorSource(List<Unit> units) {
    String line = "package generated; public class Decorator {}";
    return new Source("generated", "Decorator", Arrays.asList(line));
  }

  @Override
  public Source createExecutableSource(Unit unit) {
    List<String> lines = new ArrayList<>();
    lines.add("package " + unit.getPackageName() + ";");
    lines.add("");
    lines.add("public class " + unit.getClassName() + " {");
    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
    return new Source(unit.getPackageName(), unit.getClassName(), lines);
  }

}
