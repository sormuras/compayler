package de.sormuras.compayler.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Field;
import de.sormuras.compayler.model.Type;
import de.sormuras.compayler.model.Unit;
import de.sormuras.compayler.service.SourceFactory;

public class DefaultSourceFactory implements SourceFactory {

  public static String merge(String head, String tail, String separator, Object... objects) {
    StringBuilder builder = new StringBuilder();
    int count = 0;
    builder.append(head);
    for (Object var : objects) {
      if (count > 0)
        builder.append(separator);
      builder.append(var);
      count++;
    }
    builder.append(tail);
    return count > 0 ? builder.toString() : "";
  }

  public static String now() {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
    df.setTimeZone(tz);
    return df.format(new Date());
  }

  protected Compayler compayler;
  protected Lines lines;

  protected void addClassComment() {
    lines.add("");
    lines.add("/**");
    lines.add(" * Class %s generated for %s.", compayler.getDecoratorClassName(), compayler.getInterfaceName());
    lines.add(" *");
    lines.add(" * Generated on %s.", now());
    lines.add(" */");
  }

  protected void addDecoratorClass(List<Unit<?>> units) {
    Type closeableType = Type.forName("java.io.Closeable");
    Type interfaceType = Type.forName(compayler.getInterfaceClassName(), ""); // TODO Care about type arguments.
    StringBuilder line = new StringBuilder();
    line.append("public class ").append(compayler.getDecoratorName());
    line.append(" extends ").append(compayler.getSuperClassName());
    line.append(" implements ").append(merge("", "", ", ", closeableType, interfaceType));
    line.append(" {");
    lines.add(line.toString());

    lines.pushIndention();
    lines.add("");
    lines.add("private interface Executable {");

    lines.pushIndention();
    for (Unit<?> unit : units) {
      addExecutableClass(unit);
    }
    lines.popIndention();
    lines.add("");
    lines.add("}"); // end of interface

    // TODO add fields and c'tor
    // TODO add basic and helper methods
    // TODO add implementations using Executable classes from above

    lines.popIndention();
    lines.add("");
    lines.add("}"); // end of decorator class
  }

  protected void addExecutableClass(Unit<?> unit) {
    lines.add("");
    lines.add("class %s implements %s {", unit.generateClassNameWithTypeVariables(), unit.generateImplements());
    lines.pushIndention();
    lines.add("");
    lines.add("private static final long serialVersionUID = %dL;", unit.getSerialVersionUID());
    addExecutableClassFieldsAndConstructor(unit);
    lines.popIndention();
    lines.add("");
    lines.add("}");
  }

  protected void addExecutableClassFieldsAndConstructor(Unit<?> unit) {
    if (unit.getSignature().getFields().isEmpty())
      return;
    lines.add("");
    for (Field field : unit.getSignature().getFields()) {
      if (field.isTime())
        lines.add("@SuppressWarnings(\"unused\")");
      lines.add("private final %s %s;", field.getType().toString(false), field.getName());
    }
    lines.add("");
    lines.add("public %s%s {", compayler.getDecoratorName(), unit.generateParameterSignature());
    lines.pushIndention();
    for (Field field : unit.getSignature().getFields()) {
      lines.add("this.%s = %1$s;", field.getName());
    }
    lines.popIndention();
    lines.add("}");
  }

  protected void addPackage() {
    String name = compayler.getDecoratorPackage();
    if (name == null || name.isEmpty())
      return;
    // no empty line before package declaration
    lines.add("package %s;", name);
  }

  @Override
  public Source createSource(Compayler compayler, List<Unit<?>> descriptions) {
    this.compayler = compayler;
    this.lines = new Lines();
    addPackage();
    addClassComment();
    addDecoratorClass(descriptions);
    return new Source(compayler.getDecoratorPackage(), compayler.getDecoratorName(), lines.getLines());
  }

}
