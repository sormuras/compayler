package org.prevayler.contrib.compayler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Generate all source files.
 * 
 * @author Christian Stein
 */
public class GenerateSourcesTask<PI> implements Callable<List<Source>> {

  private final Compayler<PI> compayler;
  private final Map<Method, Tag<PI>> tags;

  protected GenerateSourcesTask(Compayler<PI> compayler) {
    this.compayler = compayler;
    this.tags = new HashMap<>();
    for (Method method : compayler.getConfiguration().getPrevalentInterface().getMethods()) {
      tags.put(method, compayler.getConfiguration().createTag(method));
    }
  }

  @Override
  public List<Source> call() {
    List<Source> sources = new LinkedList<>();
    for (Tag<PI> tag : tags.values()) {
      sources.add(compayler.generateExecutableSource(tag));
    }
    sources.add(compayler.generateDecoratorSource(tags.values()));
    return sources;
  }

  public Compayler<PI> getCompayler() {
    return compayler;
  }

  public Tag<PI> getTag(String name, Class<?>... parameterTypes) {
    try {
      Method method = compayler.getConfiguration().getPrevalentInterface().getMethod(name, parameterTypes);
      return tags.get(method);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException("", e);
    }
  }

}