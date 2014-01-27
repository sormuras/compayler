package de.sormuras.compayler.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.sormuras.compayler.Compayler;
import de.sormuras.compayler.Source;
import de.sormuras.compayler.model.Description;
import de.sormuras.compayler.model.Type;
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
    lines.add(" * Class " + compayler.getDecoratorClassName() + " generated for " + compayler.getInterfaceName() + ".");
    lines.add(" *");
    lines.add(" * Generated at " + now());
    lines.add(" */");
  }

  protected void addDecoratorClass(List<Description<?>> descriptions) {
    Type closeableType = Type.forName("java.io.Closeable");
    Type interfaceType = Type.forName(compayler.getInterfaceClassName(), ""); // TODO Care about type arguments.
    StringBuilder line = new StringBuilder();
    line.append("public class ").append(compayler.getDecoratorName());
    line.append(" extends ").append(compayler.getSuperClassName());
    line.append(" implements ").append(merge("", "", ", ", closeableType, interfaceType));
    line.append(" {");
    lines.add(line.toString()).pushIndention();

    lines.add("");
    lines.add("private interface Executable {").pushIndention();
    for (Description<?> description : descriptions) {
      addExecutableClass(description);
    }
    lines.popIndention().add("", "}");

    lines.popIndention().add("", "}");
  }

  protected void addExecutableClass(Description<?> description) {
    lines.add("");
    lines.add("class " + description.getSignature().getName().toUpperCase() + " {");
    lines.pushIndention();
    lines.add("// " + description.getSignature().getName());
    lines.popIndention();
    lines.add("}");
  }

  protected void addPackage() {
    String name = compayler.getDecoratorPackage();
    if (name == null || name.isEmpty())
      return;
    // no empty line before package declaration
    lines.add("package " + name + ";");
  }

  @Override
  public Source createSource(Compayler compayler, List<Description<?>> descriptions) {
    this.compayler = compayler;
    this.lines = new Lines();
    addPackage();
    addClassComment();
    addDecoratorClass(descriptions);
    return new Source(compayler.getDecoratorPackage(), compayler.getDecoratorName(), lines.getLines());
  }

}
