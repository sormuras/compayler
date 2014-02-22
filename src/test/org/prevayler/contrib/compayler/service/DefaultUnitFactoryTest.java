package org.prevayler.contrib.compayler.service;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.contrib.compayler.Repository;
import org.prevayler.contrib.compayler.Source;
import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Unit;

public class DefaultUnitFactoryTest implements Repository {

  public interface Interface {
    int someAction(boolean value);
  }

  @Override
  public Type getDecoratorType() {
    return new Type("abc.InterfaceDecorator");
  }

  @Override
  public Type getInterfaceType() {
    return new Type(Interface.class);
  }

  @Override
  public Type getSuperType() {
    return new Type(Object.class);
  }

  @Override
  public List<Unit> getUnits() {
    return new DefaultUnitFactory().createUnits(this);
  }

  @Test
  public void test() throws Exception {
    Source source = new DefaultSourceFactory().createSource(this);
    assertTrue(source.getCharContent(true).toString().contains("class " + getDecoratorType().getSimpleName()));
    ClassLoader loader = source.compile();
    Class<?> some = loader.loadClass(getDecoratorType().getBinaryName() + "$Executable$SomeAction");
    assertTrue(Serializable.class.isAssignableFrom(some));
    assertTrue(SureTransactionWithQuery.class.isAssignableFrom(some));
  }

}
