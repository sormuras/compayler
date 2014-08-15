package org.prevayler.contrib.compayler;

import static org.prevayler.contrib.compayler.Compayler.prevayler;

import java.io.File;

import org.prevayler.Prevayler;

public class TestTool {

  @SuppressWarnings("unchecked")
  public static <T> T decorate(Class<T> interfaceClass, T prevalentSystem, File folder) throws Exception {
    Compayler compayler = new Compayler(interfaceClass);
    Prevayler<T> prevayler = prevayler(prevalentSystem, folder);
    return (T) Class.forName(compayler.getDecoratorName()).getConstructor(Prevayler.class).newInstance(prevayler);
  }

}
