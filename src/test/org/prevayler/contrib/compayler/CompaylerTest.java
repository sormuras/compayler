package org.prevayler.contrib.compayler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.prevayler.contrib.compayler.prevayler.VolatilePrevaylerFactory;

public class CompaylerTest {

  public static class Simple implements Simplicissimus {

    private StringBuilder builder = new StringBuilder();

    @Override
    public Appendable append(CharSequence csq) throws IOException {
      return builder.append(csq);
    }

    @Override
    public Appendable append(char c) throws IOException {
      return builder.append(c);

    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      return builder.append(csq, start, end);
    }

    @Override
    public Simplicissimus chain(Date time) throws TimeoutException {
      return null;
    }

    @Override
    public int compareTo(Integer other) {
      return 0;
    }

    @Override
    public Map<String, List<String>> generateMap(Map<String, List<String>> map, String... more) {
      return null;
    }

    @Override
    public List<Integer> getNumbers() {
      return null;
    }

    @Override
    public void run() {
    }
    
    @Override
    public String toString() {
      return builder.toString();
    }

  }

  @Test
  public void testAppendableBackedByStringBuilder() throws Exception {
    Simplicissimus appendable = new Compayler(Simplicissimus.class).decorate(new VolatilePrevaylerFactory<>(new Simple()));
    assertEquals("123", appendable.append('1').append("2").append("123", 2, 3).toString());
  }

}
