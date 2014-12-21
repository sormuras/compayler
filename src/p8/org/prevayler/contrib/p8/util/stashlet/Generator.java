package org.prevayler.contrib.p8.util.stashlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.prevayler.contrib.p8.util.Stashable;
import org.prevayler.contrib.p8.util.stashlet.Context.Scope;
import org.prevayler.contrib.p8.util.stashlet.common.EnumStashlet;
import org.prevayler.contrib.p8.util.stashlet.common.ObjectStashlet;
import org.prevayler.contrib.p8.util.stashlet.common.StashableStashlet;
import org.prevayler.contrib.p8.util.stashlet.primitive.BooleanPrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.BytePrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.CharPrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.DoublePrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.FloatPrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.IntPrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.LongPrimitive;
import org.prevayler.contrib.p8.util.stashlet.primitive.ShortPrimitive;
import org.prevayler.contrib.p8.util.stashlet.wrapper.BooleanWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.ByteWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.CharacterWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.DoubleWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.FloatWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.IntegerWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.LongWrapper;
import org.prevayler.contrib.p8.util.stashlet.wrapper.ShortWrapper;

public class Generator {

  /**
   * Check template validity.
   *
   * @param type
   *          the described type
   * @param template
   *          the template to check
   * @return the passed template
   */
  public static <T extends Stashlet> T check(Class<?> type, T template) {
    if (!type.getTypeName().equals(template.getTypeName()))
      throw new IllegalArgumentException("Type name mismatch! '" + type.getTypeName() + "' is not '" + template.getTypeName() + "'");
    return template;
  }

  public static List<Stashlet> addDefaultTemplates(List<Stashlet> list) {
    // 8 primitive types (never null)
    list.add(check(boolean.class, new BooleanPrimitive()));
    list.add(check(byte.class, new BytePrimitive()));
    list.add(check(char.class, new CharPrimitive()));
    list.add(check(double.class, new DoublePrimitive()));
    list.add(check(float.class, new FloatPrimitive()));
    list.add(check(int.class, new IntPrimitive()));
    list.add(check(long.class, new LongPrimitive()));
    list.add(check(short.class, new ShortPrimitive()));

    // 8 wrapper for primitive types (nullable)
    list.add(check(Boolean.class, new BooleanWrapper()));
    list.add(check(Byte.class, new ByteWrapper()));
    list.add(check(Character.class, new CharacterWrapper()));
    list.add(check(Double.class, new DoubleWrapper()));
    list.add(check(Float.class, new FloatWrapper()));
    list.add(check(Integer.class, new IntegerWrapper()));
    list.add(check(Long.class, new LongWrapper()));
    list.add(check(Short.class, new ShortWrapper()));

    return list;
  }

  public static List<Stashlet> addProvidedTemplates(List<Stashlet> list) {
    try {
      for (Stashlet stashlet : ServiceLoader.load(Stashlet.class)) {
        System.out.println("Stashlet " + stashlet + " loaded!");
        list.add(stashlet);
      }
      return list;
    } catch (ServiceConfigurationError e) {
      throw new Error(e);
    }
  }

  private final List<Stashlet> templates;

  public Generator() {
    this.templates = new ArrayList<>(100);

    addDefaultTemplates(templates);
    addProvidedTemplates(templates);
  }

  public Generator add(Stashlet stashlet) {
    stashlet.getTypeName();
    if (templates.contains(stashlet))
      throw new IllegalStateException("Already added stashlate: " + stashlet);
    templates.add(stashlet);
    return this;
  }

  public List<String> generate(Context context) {
    Stashlet stashlet = getStashlet(context.getTypeName());
    String template = stashlet.get(context.getScope(), context.isNullable());
    for (Map.Entry<String, Object> entry : context.map().entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue().toString());
    }
    return Arrays.asList(template.split("[\r\n]+"));
  }

  public List<String> generate(Scope scope, String property, String type, boolean nullable) {
    return generate(new Context(scope, property, type, nullable));
  }

  public Stashlet getStashlet(Class<?> type) {
    return getStashlet(type.getTypeName(), type.getClassLoader());
  }

  public Stashlet getStashlet(String typeName) {
    return getStashlet(typeName, getClass().getClassLoader());
  }

  public Stashlet getStashlet(String typeName, ClassLoader loader) {
    if (typeName == null || typeName.isEmpty())
      throw new IllegalArgumentException("typeName must not be null nor empty!");

    ListIterator<Stashlet> iterator = templates.listIterator(templates.size());
    while (iterator.hasPrevious()) {
      Stashlet stashlet = iterator.previous();
      if (typeName.equals(stashlet.getTypeName()))
        return stashlet;
    }

    try {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      Stashlet stashlet = new EnumStashlet(Class.forName(typeName, true, loader));
      return stashlet;
    } catch (Exception e) {
      // ignore
    }

    try {
      if (Stashable.class.isAssignableFrom(Class.forName(typeName, true, loader))) {
        return new StashableStashlet(typeName);
      }
    } catch (Exception e) {
      // ignore
    }

    return new ObjectStashlet(typeName);
  }

}
