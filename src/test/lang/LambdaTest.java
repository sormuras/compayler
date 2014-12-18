package lang;

import static org.junit.Assert.assertEquals;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import org.junit.Test;

public class LambdaTest {

  public static interface StringFunction<N extends Number> extends Function<String, N> {

    @Override
    N apply(String t);

  }

  public static <N extends Number> StringFunction<N> create(Class<N> type) throws Throwable {
    MethodType methodType = MethodType.methodType(type, String.class);
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle handle = lookup.findConstructor(type, MethodType.methodType(void.class, String.class));
    MethodType samType = methodType.changeReturnType(Number.class);
    MethodType invokedType = MethodType.methodType(StringFunction.class);
    CallSite callsite = LambdaMetafactory.metafactory(lookup, "apply", invokedType, samType, handle, methodType);
    StringFunction<N> f = (StringFunction<N>) callsite.getTarget().invokeExact();
    return f;
  }

  @Test
  public void testStringFunction() throws Throwable {
    assertEquals(1, create(Byte.class).apply("1").intValue());
    assertEquals(2, create(Short.class).apply("2").intValue());
    assertEquals(3, create(Integer.class).apply("3").intValue());
    assertEquals(4, create(Long.class).apply("4").intValue());
  }

}
