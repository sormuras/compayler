package org.prevayler.contrib.compayler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Static utilities.
 * 
 * @author Christian Stein
 */
public final class Util {

  /**
   * Store compiled java class.
   */
  public static class JavaClassObject extends SimpleJavaFileObject {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000);

    public JavaClassObject(String name, Kind kind) {
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
      this.parent = parent;
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
    JavaFileManager fileManager = new JavaFileManager(compiler.getStandardFileManager(null, null, null), parent);
    compiler.getTask(null, fileManager, null, null, null, sources).call();
    return fileManager.getClassLoader(null);
  }

  public static void dump(Iterable<Source> sources) {
    dump(sources, System.out);
  }

  public static void dump(Iterable<Source> sources, PrintStream out) {
    for (Source source : sources) {
      out.print(source.getCharContent(false));
    }
  }

  /**
   * Returns the smallest index of the annotation class with the method parameter list.
   * 
   * @param method
   *          to analyze
   * @param annotationType
   *          to look for
   * @return the zero-based index of the annotation within the parameter list, -1 if annotationType is not present
   */
  public static int index(Method method, Class<? extends Annotation> annotationType) {
    Annotation[][] annotationsArray = method.getParameterAnnotations();
    for (int index = 0; index < annotationsArray.length; index++) {
      Annotation[] annotations = annotationsArray[index];
      for (int offset = 0; offset < annotations.length; offset++) {
        if (annotationType == annotations[offset].annotationType()) {
          return index;
        }
      }
    }
    return -1;
  }

  /**
   * Returns the canonical name of the class or, if the class belongs to "java.lang" package, the simple class name.
   * 
   * @param type
   *          the type to give the name for
   * @return the "java source" name of the type
   */
  public static String name(Class<?> type) {
    Package pack = type.getPackage();
    if (type.isArray())
      pack = type.getComponentType().getPackage();
    String packageName = null;
    if (pack != null)
      packageName = pack.getName();
    if ("java.lang".equals(packageName))
      return type.getSimpleName();
    return type.getCanonicalName();
  }

  /**
   * Returns wrapper class for the primitive type.
   * 
   * @param type
   *          the type to wrap
   * @return if primitive type return its wrapper class, else type
   */
  public static Class<?> wrap(Class<?> type) {
    // if primitive type return its wrapper class
    if (type == boolean.class)
      return Boolean.class;
    if (type == byte.class)
      return Byte.class;
    if (type == char.class)
      return Character.class;
    if (type == double.class)
      return Double.class;
    if (type == float.class)
      return Float.class;
    if (type == int.class)
      return Integer.class;
    if (type == long.class)
      return Long.class;
    if (type == short.class)
      return Short.class;
    if (type == void.class)
      return Void.class;
    // no primitive, no wrap
    return type;
  }

  public static void write(Iterable<Source> sources, String targetPath) throws Exception {
    for (Source source : sources) {
      String packname = source.getPackageName();
      String pathname = targetPath + "/" + packname.replace('.', '/');
      File parent = Files.createDirectories(new File(pathname).toPath().toAbsolutePath()).toFile();
      File file = new File(parent, source.getSimpleClassName() + source.getKind().extension);
      System.out.print(file + " ...");
      Files.write(file.toPath(), source.getLinesOfCode(), source.getCharset());
      System.out.println(" ok");
    }
  }

}
