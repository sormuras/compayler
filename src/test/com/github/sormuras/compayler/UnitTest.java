package com.github.sormuras.compayler;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.sormuras.compayler.qdox.QDoxUnitFactory;
import com.github.sormuras.compayler.reflect.ReflectUnitFactory;

public class UnitTest {

  @Test(expected = IllegalArgumentException.class)
  public void testClassNameContainsPackageName() {
    new Unit("a.A");
  }

  @Test
  public void testCreateUnitsWithQDoxUnitFactory() {
    UnitFactory factory = new QDoxUnitFactory("src/test/" + Api.class.getName().replace('.', '/') + ".java");
    List<Unit> units = factory.createUnits(Api.class.getName());
    Assert.assertNotNull(units);
    System.out.println(units);
  }

  @Test
  public void testCreateUnitsWithReflectUnitFactory() {
    UnitFactory factory = new ReflectUnitFactory(Api.class);
    List<Unit> units = factory.createUnits(Api.class.getName());
    Assert.assertNotNull(units);
    System.out.println(units);
  }

}
