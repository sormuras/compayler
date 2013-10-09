package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import org.junit.Test;
import org.prevayler.foundation.serialization.JavaSerializer;

public class JavaSerializerTest {

  @Test
  public void testJavaSerializerUsesCurrentThreadContextClassLoader() throws Exception {
    Field field = JavaSerializer.class.getDeclaredField("_loader");
    field.setAccessible(true);

    ClassLoader loader = new ClassLoader() {
      // empty class
    };
    Thread.currentThread().setContextClassLoader(loader);
    JavaSerializer serializer = new JavaSerializer();

    assertEquals(loader, Thread.currentThread().getContextClassLoader());
    assertEquals(loader, field.get(serializer));
  }

}
