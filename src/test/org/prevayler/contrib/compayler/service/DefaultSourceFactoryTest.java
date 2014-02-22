package org.prevayler.contrib.compayler.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.prevayler.SureTransactionWithQuery;
import org.prevayler.contrib.compayler.Param;
import org.prevayler.contrib.compayler.Repository;
import org.prevayler.contrib.compayler.Shape;
import org.prevayler.contrib.compayler.Source;
import org.prevayler.contrib.compayler.Type;
import org.prevayler.contrib.compayler.Unit;

public class DefaultSourceFactoryTest implements Repository {

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
    List<Unit> units = new ArrayList<>();
    List<Param> params = asList(new Param("value", new Type("boolean")));
    Shape shape = new Shape("someAction", new Type("int"), params, null, true);
    // shape.setMode(Mode.TRANSACTION);
    // shape.setSerialVersionUID(4225024575256642595L);
    units.add(shape);
    return units;
  }

  @Test
  public void test() throws Exception {
    Source source = new DefaultSourceFactory().createSource(this);
    assertTrue(source.getCharContent(true).toString().contains("class " + getDecoratorType().getSimpleName()));
    ClassLoader loader = source.compile();
    Class<?> some = loader.loadClass(getDecoratorType().getBinaryName() + "$Executable$SomeAction");
    assertTrue(Serializable.class.isAssignableFrom(some));
    assertTrue(SureTransactionWithQuery.class.isAssignableFrom(some));
    // assertEquals(4225024575256642595L, ObjectStreamClass.lookup(some).getSerialVersionUID());
    // try {
    // Field uid = some.getDeclaredField("serialVersionUID");
    // uid.setAccessible(true);
    // assertEquals(4225024575256642595L, uid.get(null));
    // } catch (NoSuchFieldException e) {
    // // ignore
    // }

    // for (SerialVersionMarker marker : getMarkers()) {
    // Class<?> c = loader.loadClass(marker.getClassName());
    // String generatedUID = "private static final long serialVersionUID = " + ObjectStreamClass.lookup(c).getSerialVersionUID() + "L;";
    // source.getCode().set(marker.getIndex(), marker.getIndentation() + generatedUID);
    // }
    // loader = source.compile();
    // some = loader.loadClass(getDecoratorType().getBinaryName() + "$Executable$SomeAction");
    // assertEquals(4225024575256642595L, ObjectStreamClass.lookup(some).getSerialVersionUID());
    // Field uid = some.getDeclaredField("serialVersionUID");
    // uid.setAccessible(true);
    // assertEquals(4225024575256642595L, uid.get(null));
  }

}
