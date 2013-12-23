package com.github.sormuras.compayler;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.sormuras.compayler.qdox.QDoxTagFactory;

public class TagTest {

  @Test(expected = IllegalArgumentException.class)
  public void testClassNameContainsPackageName() {
    List<String> empty = Collections.emptyList();
    new Tag("a.A", "", empty, empty, "", empty, true);
  }

  @Test
  public void testDefaultTagFactory() {
    TagFactory factory = new DefaultTagFactory(Api.class);
    List<Tag> tags = factory.createTags(Api.class.getName());
    Assert.assertNotNull(tags);
    System.out.println(tags);
  }

  @Test
  public void testQDoxTagFactory() {
    String[] files = { "src/test/" + Api.class.getName().replace('.', '/') + ".java",
        "src/main/com/github/sormuras/compayler/PrevalentMethod.java" };
    TagFactory factory = new QDoxTagFactory(files);
    List<Tag> tags = factory.createTags(Api.class.getName());
    Assert.assertNotNull(tags);
    System.out.println(tags);
  }

}
