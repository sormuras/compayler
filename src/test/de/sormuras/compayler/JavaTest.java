package de.sormuras.compayler;

import org.junit.Assert;
import org.junit.Test;

public class JavaTest {

  @Test
  public void testArrayOfArrayType() {
    int[][][] m3 = new int[1][1][1];
    Assert.assertTrue(m3.getClass().isArray());
    Assert.assertTrue(m3.getClass().getComponentType().isArray());
    Assert.assertTrue(m3.getClass().getComponentType().getComponentType().isArray());
    Assert.assertEquals(int.class, m3.getClass().getComponentType().getComponentType().getComponentType());
  }

}
