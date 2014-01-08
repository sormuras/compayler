package com.github.sormuras.compayler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.prevayler.Prevayler;

public class Loader {
  /**
   * Store compiled java class.
   */
  public static class JavaClassObject extends SimpleJavaFileObject {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);

    public JavaClassObject(String name, JavaFileObject.Kind kind) {
      super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
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
      JavaClassObject object = new JavaClassObject(className, kind);
      map.put(className, object);
      return object;
    }
  }

  public static ClassLoader compile(Iterable<Source> sources, ClassLoader parent) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("No system java compiler available. JDK is required!");
    }
    JavaFileManager fileManager = new JavaFileManager(compiler.getStandardFileManager(null, null, null), parent);
    List<String> options = new ArrayList<>();
    options.add("-Xlint:all");
    compiler.getTask(null, fileManager, null, options, null, sources).call();
    return fileManager.getClassLoader(null);
  }

  @SuppressWarnings("unchecked")
  public static <P> P decorate(Source source, Prevayler<P> prevayler) throws Exception {
    ClassLoader loader = compile(Arrays.asList(source), Loader.class.getClassLoader());
    Class<?> decoratorClass = loader.loadClass(source.getPackageName() + "." + source.getSimpleClassName());
    return (P) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

}
