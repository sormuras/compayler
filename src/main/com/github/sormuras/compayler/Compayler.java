package com.github.sormuras.compayler;

import java.util.ArrayList;
import java.util.List;

public class Compayler {

//  private final UnitFactory tagFactory;
//  private final TagFactory unitFactory;
//
//  public Compayler(UnitFactory tagFactory, TagFactory unitFactory) {
//    this.tagFactory = tagFactory;
//    this.unitFactory = unitFactory;
//  }

//  /**
//   * @return list of tags as created by the configured tag factory
//   */
//  public List<Tag> createTags(String className) {
//    List<Tag> units = unitFactory.createTags(className);
//    return units;
//  }
//
//  /**
//   * @return list of units as created by the configured unit factory
//   */
//  public List<Unit> createUnits(List<Tag> units) {
//    List<Unit> tags = new ArrayList<>(units.size());
//    for (Tag unit : units) {
//      tags.add(tagFactory.createUnit(unit));
//    }
//    return tags;
//  }

  public List<String> compile(Unit unit) {
    List<String> lines = new ArrayList<>();
    
    lines.add("package " + unit.getPackageName() + ";");
    lines.add("");
    lines.add("public class " + unit.getClassName()); //  + " implements " + tag.getExecutableDeclaration(configuration) + " {");
    lines.add("");
//    lines.add("  private static final long serialVersionUID = " + tag.getSerialVersionUID() + "L;");
//    lines.add("");
    
//    // fields + c'tor, if at least one parameter is present
//    if (tag.getMethod().getParameterTypes().length > 0) {
//      int index = 0;
//      for (Class<?> param : tag.getMethod().getParameterTypes()) {
//        if (index == tag.getIndexOfPrevalentDate())
//          lines.add("  @SuppressWarnings(\"unused\")");
//        lines.add("  private final " + Util.name(param) + tag.getParameterDescriptors()[index].getValue(Tag.TYPE_ARGS) + " "
//            + tag.getParameterName(index) + ";");
//        index++;
//      }
//      lines.add("");
//      lines.add("  public " + tag.getClassName() + tag.getParameterSignature() + " {");
//      for (int i = 0; i < tag.getMethod().getParameterTypes().length; i++)
//        lines.add("    this." + tag.getParameterName(i) + " = " + tag.getParameterName(i) + ";");
//      lines.add("  }");
//      lines.add("");
//    }
    
//    // implementation
//    String parameters = Util.name(prevalentInterface) + configuration.getPrevalentInterfaceTypeArguments()
//        + " prevalentSystem, java.util.Date executionTime";
//    String methodCall = tag.getMethod().getName() + tag.getParameterParenthesesWithExecutionTime();
//    lines.add("  @Override");
//    if (tag.getExecutableClass() == Query.class) {
//      lines.add("  public " + tag.getReturnTypeWrapName() + " query(" + parameters + ") throws Exception {");
//      lines.add("    return prevalentSystem." + methodCall + ";");
//    }
//    if (tag.getExecutableClass() == Transaction.class) {
//      lines.add("  public void executeOn(" + parameters + ") {");
//      lines.add("    prevalentSystem." + methodCall + ";");
//    }
//    if (tag.getExecutableClass() == TransactionWithQuery.class) {
//      lines.add("  public " + tag.getReturnTypeWrapName() + " executeAndQuery(" + parameters + ") throws Exception {");
//      lines.add("    return prevalentSystem." + methodCall + ";");
//    }
//    if (tag.getExecutableClass() == SureTransactionWithQuery.class) {
//      lines.add("  public " + tag.getReturnTypeWrapName() + " executeAndQuery(" + parameters + ") {");
//      lines.add("    return prevalentSystem." + methodCall + ";");
//    }
//    lines.add("  }"); // end of method
    lines.add("");
    lines.add("}"); // end of class
    lines.add("");
    
    
    // return new Source(unit.getPackageName(), unit.getClassName(), lines);
    return lines;
  }

}
