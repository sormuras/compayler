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

    // fields + c'tor, if at least one parameter is present
    if (unit.hasParameters()) {
      lines.add("");
      int index = 0;
      for (String type : unit.getTag().getParameterTypes()) {
        if (type.endsWith("..."))
          type = type.substring(0, type.length() - 3) + "[]";
        if (index == unit.getTag().getPrevalentTime())
          lines.add("  @SuppressWarnings(\"unused\")");
        lines.add("  private final " + type + " " + unit.getTag().getParameterNames().get(index) + ";");
        index++;
      }
      lines.add("");
      lines.add("  public " + unit.getClassName() + unit.getParameterSignature() + " {");
      for (int i = 0; i < unit.getTag().getParameterTypes().size(); i++) {
        String name = unit.getTag().getParameterNames().get(i);
        lines.add("    this." + name + " = " + name + ";");
      }
      lines.add("  }");
    } // end of fields + c'tor

    // implementation
    String executableInterface = getImplements(unit);
    executableInterface = executableInterface.substring(0, executableInterface.indexOf('<'));
    String parameters = interfaceName + " prevalentSystem, java.util.Date executionTime";
    String methodCall = unit.getTag().getName() + unit.getParameterParenthesesWithExecutionTime();
    lines.add("");
    lines.add("  @Override");
    switch (executableInterface) {
    case "org.prevayler.Query":
      lines.add("  public " + unit.getReturnType() + " query(" + parameters + ") throws Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    case "org.prevayler.Transaction":
      lines.add("  public void executeOn(" + parameters + ") {");
      lines.add("    prevalentSystem." + methodCall + ";");
      break;
    case "org.prevayler.TransactionWithQuery":
      lines.add("  public " + unit.getReturnType() + " executeAndQuery(" + parameters + ") throws Exception {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    case "org.prevayler.SureTransactionWithQuery":
      lines.add("  public " + unit.getReturnType() + " executeAndQuery(" + parameters + ") {");
      lines.add("    return prevalentSystem." + methodCall + ";");
      break;
    default:
      // TODO handle this error case
      break;
    }
    lines.add("  }"); // end of implementation

    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
    return new Source(unit.getPackageName(), unit.getClassName(), lines);
  }

  // TODO move to unit factory
  public String getImplements(Unit unit) {
    Tag tag = unit.getTag();
    String typed = "<" + interfaceName + ", " + unit.getReturnType() + ">";
    if (tag.getPrevalentType() == PrevalentType.QUERY)
      return "org.prevayler.Query" + typed;
    if (tag.getReturnType().equals("void"))
      return "org.prevayler.Transaction" + "<" + interfaceName + ">";
    if (tag.getThrowing().isEmpty())
      return "org.prevayler.SureTransactionWithQuery" + typed;
    return "org.prevayler.TransactionWithQuery" + typed;
  }

}
