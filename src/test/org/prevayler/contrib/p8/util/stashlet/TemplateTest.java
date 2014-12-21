package org.prevayler.contrib.p8.util.stashlet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.prevayler.contrib.p8.util.stashlet.Context.Scope.SPAWN;
import static org.prevayler.contrib.p8.util.stashlet.Context.Scope.STASH;

import org.junit.Test;
import org.prevayler.contrib.p8.util.stashlet.common.AbstractStashlet;
import org.prevayler.contrib.p8.util.stashlet.common.EnumStashlet;

public class TemplateTest {

  public static class Example extends AbstractStashlet {

    public Example() {
      super("123");
    }

    @Override
    public String getSpawnNullable() {
      return "";
    }

    @Override
    public String getSpawnStraight() {
      return "";
    }

    @Override
    public String getStashNullable() {
      return "";
    }

    @Override
    public String getStashStraight() {
      return "";
    }

  }

  Stashlet out(Stashlet stashlet) {
    String type = stashlet.getTypeName();
    System.out.println("\n\"" + type + "\" -> " + stashlet.getClass());
    System.out.println("  (spawn straight) " + generator.generate(SPAWN, "data", type, false));
    System.out.println("  (stash straight) " + generator.generate(STASH, "data", type, false));
    if (stashlet.isTypeNullable()) {
      System.out.println("  (spawn nullable) " + generator.generate(SPAWN, "data", type, true));
      System.out.println("  (stash nullable) " + generator.generate(STASH, "data", type, true));
    }
    return stashlet;
  }

  private Generator generator = new Generator();

  @Test
  public void testDefaults() throws Exception {
    // Generator.createDefaultTemplates().forEach(this::out);

    // out(new StashableStashlet(Data.class.getTypeName()));
    // out(new EnumStashlet<>(Thread.State.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnumBuilder() throws Exception {
    assertNotNull(new EnumStashlet<>(Thread.State.class));
    @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    EnumStashlet fails = new EnumStashlet(Class.forName("java.lang.Object"));
    fail();
  }

  @Test
  public void testJavaClasses() throws Exception {
    assertNotNull(generator.getStashlet(Boolean.class));
    assertNotNull(generator.getStashlet(Byte.class));
    assertNotNull(generator.getStashlet(Character.class));
    assertNotNull(generator.getStashlet(Double.class));
    assertNotNull(generator.getStashlet(Float.class));
    assertNotNull(generator.getStashlet(Integer.class));
    assertNotNull(generator.getStashlet(Long.class));
    assertNotNull(generator.getStashlet(Short.class));
  }

  @Test
  public void testJavaPrimitives() throws Exception {
    assertNotNull(generator.getStashlet(boolean.class));
    assertNotNull(generator.getStashlet(byte.class));
    assertNotNull(generator.getStashlet(char.class));
    assertNotNull(generator.getStashlet(double.class));
    assertNotNull(generator.getStashlet(float.class));
    assertNotNull(generator.getStashlet(int.class));
    assertNotNull(generator.getStashlet(long.class));
    assertNotNull(generator.getStashlet(short.class));
  }

  @Test
  public void testGenericEnumBuilder() throws Exception {
    assertNotNull(generator.getStashlet(java.util.Locale.Category.class));
    assertNotNull(generator.getStashlet(java.math.RoundingMode.class));
    assertNotNull(generator.getStashlet(java.util.Locale.Category.class.getTypeName()));
  }

  @Test
  public void testServiceProvider() throws Exception {
    assertNotNull(generator.getStashlet(Example.class));
  }

}
