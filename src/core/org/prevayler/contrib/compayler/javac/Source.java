package org.prevayler.contrib.compayler.javac;

import static java.util.Collections.singleton;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Java source file object implementation.
 * 
 * @author Christian Stein
 */
public class Source extends SimpleJavaFileObject {

  @FunctionalInterface
  public interface TaskVisitor {

    void visitTask(CompilationTask task);

  }

  private List<String> lines;

  public Source(String canonical, List<String> lines) {
    super(URI.create("source:///" + canonical.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.lines = lines;
  }

  public ClassLoader compile() {
    return compile(System::identityHashCode, getClass().getClassLoader());
  }

  public ClassLoader compile(Processor processor) {
    return compile(task -> task.setProcessors(singleton(processor)), getClass().getClassLoader());
  }

  public ClassLoader compile(TaskVisitor taskVisitor, ClassLoader parent) {
    Objects.requireNonNull(taskVisitor);
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("No system java compiler available. JDK is required!");
    }
    Manager manager = new Manager(compiler.getStandardFileManager(null, null, null), parent);
    List<String> options = new ArrayList<>();
    options.add("-parameters");
    // options.add("-Xlint:all");
    // options.add("-XprintRounds");
    CompilationTask task = compiler.getTask(null, manager, null, options, null, singleton(this));
    taskVisitor.visitTask(task);
    task.call();
    return manager.getClassLoader(null);
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    StringBuilder builder = new StringBuilder(lines.size() * 80);
    lines.forEach(line -> builder.append(line).append(System.lineSeparator()));
    return builder.toString();
  }

  public Charset getCharset() {
    return Charset.forName("UTF-8");
  }

  public List<String> getLines() {
    return lines;
  }

  public Path save() throws Exception {
    return save(System.getProperty("user.dir"));
  }

  public Path save(String targetPath) throws Exception {
    File base = new File(targetPath);
    File file = new File(base, toUri().getPath());
    file.getParentFile().mkdirs();
    return Files.write(file.toPath(), getLines(), getCharset());
  }

}
