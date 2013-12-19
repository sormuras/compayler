package com.github.sormuras.compayler;

import org.junit.Assert;
import org.junit.Test;

public class UnitTest {

  @Test
  public void testDefaultUnitFactory() {
    Tag tag = new Tag("test", true);
    UnitFactory factory = new DefaultUnitFactory();
    Unit unit = factory.createUnit(tag);
    Assert.assertEquals("TestTransaction", unit.getClassName());
  }

}
