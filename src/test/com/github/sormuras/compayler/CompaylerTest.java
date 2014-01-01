package com.github.sormuras.compayler;

import java.util.List;

import org.junit.Test;

import com.github.sormuras.compayler.qdox.QDoxTagFactory;

public class CompaylerTest {

  @Test
  public void testDefaultTagFactory() {
    String interfaceName = "com.github.sormuras.compayler.Api";

    // TagFactory tagFactory = new DefaultTagFactory(com.github.sormuras.compayler.Api.class);
    TagFactory tagFactory = new QDoxTagFactory("src/test/" + interfaceName.replace('.', '/') + ".java");
    UnitFactory unitFactory = new DefaultUnitFactory();
    SourceFactory sourceFactory = new DefaultSourceFactory(interfaceName);

    List<Tag> tags = tagFactory.createTags(interfaceName);
    for (Tag tag : tags) {
      Unit unit = unitFactory.createUnit(tag);
      Source source = sourceFactory.createExecutableSource(unit);
      System.out.println(source.getCharContent(true));
    }
  }

}
