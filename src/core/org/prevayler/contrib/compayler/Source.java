package org.prevayler.contrib.compayler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Java source file object implementation.
 * 
 * @author Christian Stein
 */
public class Source extends SimpleJavaFileObject {

  /**
   * Store compiled java class.
   */
  public static class JavaClassObject extends SimpleJavaFileObject {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);

    public JavaClassObject(URI uri) {
      super(uri, Kind.SOURCE);
    }

    public byte[] getBytes() {
      return outputStream.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
      return outputStream;
    }

  }

  /**
   * Java file manager handling JavaClassObjects.
   */
  public static class JavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    private Map<String, JavaClassObject> map = new HashMap<>();
    private final ClassLoader parent;

    public JavaFileManager(StandardJavaFileManager standardManager, ClassLoader parent) {
      super(standardManager);
      this.parent = parent != null ? parent : getClass().getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
      return new SecureClassLoader(parent) {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
          JavaClassObject object = map.get(name);
          if (object == null)
            throw new ClassNotFoundException(name);
          byte[] b = object.getBytes();
          return super.defineClass(name, b, 0, b.length);
        }
      };
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
        throws IOException {
      JavaClassObject object = new JavaClassObject(URI.create(className));
      map.put(className, object);
      return object;
    }
  }

  /**
   * To be compiled.
   */
  private List<String> code;

  /**
   * This constructor will store the source code and register it as a source code, using a URI containing the class full name.
   * 
   * @param linesOfCode
   *          source code to compile
   */
  public Source(URI uri, List<String> code) {
    super(uri, Kind.SOURCE);
    this.code = code;
  }

  public ClassLoader compile() {
    return compile(Thread.currentThread().getContextClassLoader());
  }

  public ClassLoader compile(ClassLoader parent) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("No system java compiler available. JDK is required!");
    }
    JavaFileManager fileManager = new JavaFileManager(compiler.getStandardFileManager(null, null, null), parent);
    List<String> options = new ArrayList<>();
    options.add("-Xlint:all");
    List<Source> sources = new ArrayList<>();
    sources.add(this);
    compiler.getTask(null, fileManager, null, options, null, sources).call();
    return fileManager.getClassLoader(null);
  }

  /**
   * Answers the CharSequence to be compiled.
   */
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    StringBuilder builder = new StringBuilder();
    for (String line : code) {
      builder.append(line).append(System.lineSeparator());
    }
    return builder.toString();
  }

  public List<String> getCode() {
    return code;
  }

  public Charset getCharset() {
    return Charset.forName("UTF-8");
  }

  public Path save(String targetPath) throws Exception {
    File base = new File(targetPath); // System.getProperty("user.dir");
    File file = new File(base, uri.getPath());
    file.getParentFile().mkdirs();
    return Files.write(file.toPath(), getCode(), getCharset());
  }

}
