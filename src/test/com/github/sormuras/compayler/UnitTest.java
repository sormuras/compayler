package com.github.sormuras.compayler;

import static java.util.Arrays.asList;

import org.junit.Assert;
import org.junit.Test;

public class UnitTest {

  DefaultUnitFactory factory = new DefaultUnitFactory();

  @Test
  public void testDefaultUnitFactory() {
    Assert.assertEquals("TestTransaction", factory.createUnit(new Tag("test", true)).getClassName());
    Assert.assertEquals("TestTransaction_0", factory.createUnit(new Tag("test", false)).getClassName());
  }

  @Test
  public void testHashOfTypeNames() {
    Assert.assertEquals("136S0U", factory.buildHashOfTypeNames(asList("java.util.List")));
    Assert.assertEquals("IK3GX8", factory.buildHashOfTypeNames(asList("java.util.List<java.util.concurrent.TimeUnit>")));
  }

}
