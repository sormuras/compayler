package org.prevayler.contrib.compayler;

import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.implementation.snapshot.NullSnapshotManager;

public class CustomClassLoaderTest {

  @Test
  @Ignore("it works persistent")
  public void test0() throws Exception {
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);
    try (Decorator<Appendable, StringBuilder> decorator = compayler.decorate(new StringBuilder())) {
      decorator.asPrevalentInterface().append("test").append('0');
    }
  }

  @Test
  @Ignore("it works transient - but produces a 'Prevalence' folder in current working directory")
  public void test1() throws Exception {
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);
    GenerateSourcesTask<Appendable, StringBuilder> task = compayler.generateSourcesTask();
    ClassLoader loader = Util.compile(task.call(), getClass().getClassLoader());

    PrevaylerFactory<StringBuilder> factory = new PrevaylerFactory<>();
    factory.configurePrevalentSystem(new StringBuilder());
    factory.configureJournalSerializer(new JavaSerializer(loader));
    factory.configureSnapshotSerializer(new JavaSerializer(loader));
    factory.configureTransientMode(true);
    Prevayler<StringBuilder> transientPrevayler = factory.create();

    Appendable appendable = (Appendable) compayler.decorate(transientPrevayler, loader);
    appendable.append("test").append('1');
  }

  /**
   * <pre>
   * java.lang.Error: Unable to deserialize transaction
   *   at org.prevayler.implementation.Capsule.deserialize(Capsule.java:47)
   *   at org.prevayler.implementation.Capsule.executeOn(Capsule.java:60)
   *   at org.prevayler.implementation.PrevalentSystemGuard.receive(PrevalentSystemGuard.java:73)
   *   at org.prevayler.implementation.publishing.AbstractPublisher.notifySubscribers(AbstractPublisher.java:42)
   *   at org.prevayler.implementation.publishing.CentralPublisher.notifySubscribers(CentralPublisher.java:82)
   *   at org.prevayler.implementation.publishing.CentralPublisher.publishWithoutWorryingAboutNewSubscriptions(CentralPublisher.java:62)
   *   at org.prevayler.implementation.publishing.CentralPublisher.publish(CentralPublisher.java:46)
   *   at org.prevayler.implementation.PrevaylerImpl.publish(PrevaylerImpl.java:68)
   *   at org.prevayler.implementation.PrevaylerImpl.execute(PrevaylerImpl.java:79)
   *   at appendable.AppendableDecorator.append(AppendableDecorator.java:54)
   *   at org.prevayler.contrib.compayler.CustomClassLoaderTest.test2(CustomClassLoaderTest.java:63)
   *   at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   *   at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
   *   at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
   *   at java.lang.reflect.Method.invoke(Method.java:606)
   *   at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
   *   at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
   *   at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:44)
   *   at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
   *   at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
   *   at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
   *   at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
   *   at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
   *   at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
   *   at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
   *   at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
   *   at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
   *   at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
   *   at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
   *   at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
   *   at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
   *   at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
   *   at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
   *   at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
   * Caused by: java.lang.ClassNotFoundException: appendable.AppendTransaction_HKFHPX
   *   at java.net.URLClassLoader$1.run(URLClassLoader.java:366)
   *   at java.net.URLClassLoader$1.run(URLClassLoader.java:355)
   *   at java.security.AccessController.doPrivileged(Native Method)
   *   at java.net.URLClassLoader.findClass(URLClassLoader.java:354)
   *   at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
   *   at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:308)
   *   at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
   *   at java.lang.Class.forName0(Native Method)
   *   at java.lang.Class.forName(Class.java:270)
   *   at java.io.ObjectInputStream.resolveClass(ObjectInputStream.java:623)
   *   at org.prevayler.foundation.ObjectInputStreamWithClassLoader.resolveClass(ObjectInputStreamWithClassLoader.java:25)
   *   at java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:1610)
   *   at java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1515)
   *   at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1769)
   *   at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1348)
   *   at java.io.ObjectInputStream.readObject(ObjectInputStream.java:370)
   *   at org.prevayler.foundation.serialization.JavaSerializer.readObject(JavaSerializer.java:34)
   *   at org.prevayler.implementation.Capsule.deserialize(Capsule.java:45)
   *   ... 33 more
   * </pre>
   */
  @Test
  public void test2() throws Exception {
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);
    GenerateSourcesTask<Appendable, StringBuilder> task = compayler.generateSourcesTask();
    ClassLoader loader = Util.compile(task.call(), getClass().getClassLoader());

    StringBuilder newPrevalentSystem = new StringBuilder();
    PrevaylerFactory<StringBuilder> factory = new PrevaylerFactory<>();
    factory.configurePrevalentSystem(newPrevalentSystem);
    factory.configureTransientMode(true);
    NullSnapshotManager<StringBuilder> nsm = new NullSnapshotManager<>(newPrevalentSystem, "No shots!");
    Method method = PrevaylerFactory.class.getDeclaredMethod("configureNullSnapshotManager", NullSnapshotManager.class);
    method.setAccessible(true);
    method.invoke(factory, nsm);
    Prevayler<StringBuilder> transientPrevayler = factory.create();

    Appendable appendable = (Appendable) compayler.decorate(transientPrevayler, loader);
    appendable.append("test").append('2');
  }

  @Test
  // @Ignore("it works transient - doesn't deep copy transactions")
  public void test3() throws Exception {
    Compayler<Appendable, StringBuilder> compayler = new Compayler<>(Appendable.class, StringBuilder.class);
    GenerateSourcesTask<Appendable, StringBuilder> task = compayler.generateSourcesTask();
    ClassLoader loader = Util.compile(task.call(), getClass().getClassLoader());

    StringBuilder newPrevalentSystem = new StringBuilder();
    PrevaylerFactory<StringBuilder> factory = new PrevaylerFactory<>();
    factory.configurePrevalentSystem(newPrevalentSystem);
    factory.configureTransientMode(true);
    factory.configureTransactionDeepCopy(false); // only change regarding test2()
    NullSnapshotManager<StringBuilder> nsm = new NullSnapshotManager<>(newPrevalentSystem, "No shots!");
    Method method = PrevaylerFactory.class.getDeclaredMethod("configureNullSnapshotManager", NullSnapshotManager.class);
    method.setAccessible(true);
    method.invoke(factory, nsm);
    Prevayler<StringBuilder> transientPrevayler = factory.create();

    Appendable appendable = (Appendable) compayler.decorate(transientPrevayler, loader);
    appendable.append("test").append('3');
  }

}
