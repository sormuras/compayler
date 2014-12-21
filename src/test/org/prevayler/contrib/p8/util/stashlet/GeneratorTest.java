package org.prevayler.contrib.p8.util.stashlet;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.prevayler.contrib.p8.util.stashlet.ClassDescription.Param;
import org.prevayler.contrib.p8.util.stashlet.Context.Scope;

public class GeneratorTest {

  @Test
  public void test() {
    test(new ClassDescription("Data", //
        new Param("b", "boolean", false), //
        new Param("i", "int", false), //
        new Param("d", "java.lang.Double"), //
        new Param("u", "java.util.concurrent.TimeUnit", false),//
        new Param("list", "java.util.BitSet"),//
        new Param("alpha", "de.codeturm.nio.StashableTest$Alpha") //
    ));
  }

  public void test(ClassDescription description) {
    Generator generator = new Generator();
    List<String> lines = new LinkedList<>();

    lines.add("public class Data implements de.codeturm.nio.Stashable {");

    lines.add("");
    for (Param param : description.params) {
      lines.add("  public final " + param.type.replace('$', '.') + " " + param.name + ";");
    }

    lines.add("");
    lines.add("public " + description.name + "(java.nio.ByteBuffer source) {");
    for (Param param : description.params) {
      Context context = new Context(Scope.SPAWN, param.name, param.type, param.nullable);
      generator.generate(context).forEach(line -> lines.add("  " + line));
    }
    lines.add("}");

    lines.add("");
    lines.add("@Override");
    lines.add("public java.nio.ByteBuffer stash(java.nio.ByteBuffer target) {");
    for (Param param : description.params) {
      Context context = new Context(Scope.STASH, param.name, param.type, param.nullable);
      generator.generate(context).forEach(line -> lines.add("  " + line));
    }
    lines.add("  return target;");
    lines.add("}");

    lines.add("");
    lines.add("}");

    // lines.forEach(System.out::println);
  }

}
