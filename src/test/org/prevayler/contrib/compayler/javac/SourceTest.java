package org.prevayler.contrib.compayler.javac;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.RELEASE_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Util;

public class SourceTest {

  @Retention(RUNTIME)
  public @interface Tag {
    String value() default "<not set>";
  }

  private class TagProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
      return singleton(Tag.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
      if (roundEnv.processingOver())
        return true;

      // annotations.forEach(System.out::println);

      for (Element annotated : roundEnv.getElementsAnnotatedWith(Decorate.class)) {
        if (!annotated.getKind().isInterface())
          throw new IllegalStateException(Decorate.class + " expects an interface as target!");

        TypeElement interfaceElement = (TypeElement) annotated;
        Decorate decorate = interfaceElement.getAnnotation(Decorate.class);
        // System.out.println(decorate);

        try {
          JavaFileObject jfo = processingEnv.getFiler().createSourceFile(decorate.value(), interfaceElement);
          try (BufferedWriter bw = new BufferedWriter(jfo.openWriter())) {
            bw.write("public class " + Util.simple(decorate.value()) + " {}");
            bw.newLine();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }

      return true;
    }
  }

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testCompilation() throws Exception {
    Long expected = 1701L;

    List<String> lines = new ArrayList<>();
    lines.add("package test;");
    lines.add("public class Test implements java.util.concurrent.Callable<Long> {");
    lines.add("  public Long call() {");
    lines.add("    return Long.valueOf(" + expected + ");");
    lines.add("  }");
    lines.add("}");

    String className = "test.Test";
    Source source = new Source(className, lines);
    ClassLoader loader = source.compile();
    @SuppressWarnings("unchecked")
    Class<Callable<Long>> test = (Class<Callable<Long>>) loader.loadClass(className);
    assertEquals(expected, test.newInstance().call());
    Path path = source.save(temp.newFolder().getAbsolutePath());
    assertEquals(lines, Files.readAllLines(path, source.getCharset()));
  }

  @Test
  public void testCompilationWithProcessor() throws Exception {
    List<String> lines = new ArrayList<>();
    lines.add("@" + Tag.class.getCanonicalName() + "(\"Deco\")");
    lines.add("public interface Test extends java.util.concurrent.Callable<Long> {");
    lines.add("  @Override");
    lines.add("  Long call();");
    lines.add("}");

    Source source = new Source("Test", lines);
    ClassLoader loader = source.compile(new TagProcessor());
    @SuppressWarnings("unchecked")
    Class<Callable<Long>> test = (Class<Callable<Long>>) loader.loadClass("Test");
    assertTrue(test.isInterface());
    Tag tag = test.getAnnotation(Tag.class);
    assertNotNull(tag);
    assertEquals("Deco", tag.value());
  }

}
