package org.prevayler.contrib.compayler.service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.prevayler.contrib.compayler.Generator;
import org.prevayler.contrib.compayler.Param;
import org.prevayler.contrib.compayler.Shape;
import org.prevayler.contrib.compayler.Type;

public class DefaultGeneratorTest {

  private Shape shape(String name) {
    return shape(name, new Type(void.class));
  }

  private Shape shape(String name, Type returnType, Param... params) {
    return new Shape(name, returnType, Arrays.asList(params), null, true);
  }

  @Test
  public void testMethodDeclaration() {
    Generator generator = new DefaultGenerator();
    assertEquals("public void run()", generator.generateMethodDeclaration(shape("run")));
    assertEquals("public my.Result calc()", generator.generateMethodDeclaration(shape("calc", new Type("my.Result"))));
    Type i = new Type(int.class);
    assertEquals("public int add(int a, int b)", generator.generateMethodDeclaration(shape("add", i, new Param("a", i), new Param("b", i))));
    Type j = new Type(int[].class);
    assertEquals("public int[] add(int[] a, int... b)",
        generator.generateMethodDeclaration(shape("add", j, new Param("a", j), new Param("b", j, true))));
  }

}
