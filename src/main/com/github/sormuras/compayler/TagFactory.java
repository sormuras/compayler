package com.github.sormuras.compayler;

import java.util.List;
import java.util.Map;

public interface TagFactory {

  Map<String, Boolean> createUniques(String className);

  List<Tag> createTags(String className);

}
