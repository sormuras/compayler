package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultSourceFactory implements SourceFactory {

  private final String interfaceName;

  public DefaultSourceFactory(String interfaceName) {
    this.interfaceName = interfaceName;
  }

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
    lines.add("public class " + unit.getClassName() + " implements " + getImplements(unit) + " {");
    lines.add("");
    lines.add("  private static final long serialVersionUID = " + unit.getSerialVersionUID() + "L;");
    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
    return new Source(unit.getPackageName(), unit.getClassName(), lines);
  }

  public String getImplements(Unit unit) {
    Tag tag = unit.getTag();
    String simple = "<" + interfaceName + ">";
    String typed = "<" + interfaceName + ", " + wrap(tag.getReturnType()) + ">";
    if (tag.getPrevalentType() == PrevalentType.QUERY)
      return "org.prevayler.Query" + typed;
    if (tag.getReturnType().equals("void"))
      return "org.prevayler.Transaction" + simple;
    if (tag.getThrowing().isEmpty())
      return "org.prevayler.SureTransactionWithQuery" + typed;
    return "org.prevayler.TransactionWithQuery" + typed;
  }

  public String wrap(String className) {
    switch (className) {
    case "boolean":
      return "java.lang.Boolean";
    case "byte":
      return "java.lang.Byte";
    case "char":
      return "java.lang.Character";
    case "double":
      return "java.lang.Double";
    case "float":
      return "java.lang.Float";
    case "int":
      return "java.lang.Integer";
    case "long":
      return "java.lang.Long";
    case "short":
      return "java.lang.Short";
    case "void":
      return "java.lang.Void";
    default:
      return className;
    }
  }

}
