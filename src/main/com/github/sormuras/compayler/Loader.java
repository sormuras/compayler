package com.github.sormuras.compayler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import org.prevayler.Clock;
import org.prevayler.Prevayler;
import org.prevayler.foundation.monitor.Monitor;
import org.prevayler.foundation.monitor.SimpleMonitor;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.PrevaylerDirectory;
import org.prevayler.implementation.PrevaylerImpl;
import org.prevayler.implementation.clock.MachineClock;
import org.prevayler.implementation.journal.Journal;
import org.prevayler.implementation.journal.PersistentJournal;
import org.prevayler.implementation.publishing.CentralPublisher;
import org.prevayler.implementation.publishing.TransactionPublisher;
import org.prevayler.implementation.snapshot.GenericSnapshotManager;

import com.github.sormuras.compayler.Compayler.Configuration;

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

  public static ClassLoader compile(Class<?> interfaceClass) {
    Configuration configuration = new Configuration(interfaceClass.getCanonicalName());
    Scribe scribe = new Scribe(configuration);
    List<Description> descriptions = scribe.createDescriptions();
    Source source = scribe.writeDecorator(descriptions);
    return compile(Arrays.asList(source), Thread.currentThread().getContextClassLoader());
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

  public static <P> P load(Class<P> interfaceClass, P prevalentSystem) {
    Configuration configuration = new Configuration(interfaceClass.getCanonicalName());
    ClassLoader classLoader = compile(interfaceClass);
    Prevayler<P> prevayler = newPrevayler(prevalentSystem, classLoader);
    try {
      return load(classLoader, configuration.getTargetPackage() + "." + configuration.getDecoratorName(), prevayler);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <P> P load(ClassLoader loader, String decoratorClassName, Prevayler<P> prevayler) throws Exception {
    Class<?> decoratorClass = loader.loadClass(decoratorClassName);
    return (P) decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
  }

  public static <P> Prevayler<P> newPrevayler(P prevalentSystem, ClassLoader classLoader) {
    Clock clock = new MachineClock();
    Monitor monitor = new SimpleMonitor(System.err);
    Serializer serializer = new JavaSerializer(classLoader);
    try {
      PrevaylerDirectory directory = new PrevaylerDirectory("PrevalenceBase");
      Journal journal = new PersistentJournal(directory, 0, 0, true, "journal", monitor);
      GenericSnapshotManager<P> snapshotManager = new GenericSnapshotManager<P>(Collections.singletonMap("snapshot", serializer),
          "snapshot", prevalentSystem, directory, serializer);
      TransactionPublisher publisher = new CentralPublisher(clock, journal);
      return new PrevaylerImpl<P>(snapshotManager, publisher, serializer, true);
    } catch (Exception e) {
      throw new Error(e);
    }
  }

}
