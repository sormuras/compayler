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
public class GenerateSourcesTask<PI, P extends PI> implements Callable<List<Source>> {

  private final Compayler<PI, P> compayler;
  private final Map<Method, Tag> tags;

  protected GenerateSourcesTask(Compayler<PI, P> compayler) {
    this.compayler = compayler;
    this.tags = new HashMap<>();
    for (Method method : compayler.getConfiguration().getPrevalentInterface().getMethods()) {
      tags.put(method, compayler.getConfiguration().createTag(method));
    }
  }

  @Override
  public List<Source> call() {
    List<Source> sources = new LinkedList<>();
    for (Tag tag : tags.values()) {
      sources.add(compayler.generateExecutableSource(tag));
    }
    sources.add(compayler.generateDecoratorSource(tags.values()));
    return sources;
  }

  public Compayler<PI, P> getCompayler() {
    return compayler;
  }

  public Tag getTag(String name, Class<?>... parameterTypes) {
    try {
      Method method = compayler.getConfiguration().getPrevalentInterface().getMethod(name, parameterTypes);
      return tags.get(method);
    } catch (SecurityException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      Method candidate = null;
      for (Method method : compayler.getConfiguration().getPrevalentInterface().getMethods()) {
        if (!method.getName().equals(name))
          continue;
        if (candidate != null)
          throw new RuntimeException("Ambigous method name \"" + name + "\" - provide signature, i.e. parameter types!", e);
        candidate = method;
      }
      return tags.get(candidate);
    }

  }

}