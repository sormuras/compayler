package de.sormuras.compayler;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.sormuras.compayler.Compayler.CompaylerHint;
import de.sormuras.compayler.Compayler.ExecutionTime;
import de.sormuras.compayler.Compayler.Mode;

public class CompaylerTest {

  public static class Implementation implements Interface {

    @Override
    public Interface direct() {
      return this;
    }

    @Override
    public long executionTime(Date time) {
      return time.getTime();
    }

    @Override
    public Date executionTime(Date seed, Date time, Date... dates) {
      return time;
    }

  }

  public static interface Interface {

    @CompaylerHint(Mode.DIRECT)
    Interface direct();

    long executionTime(@ExecutionTime Date time);

    Date executionTime(Date seed, @ExecutionTime Date time, Date... dates);

  }

  @Test
  public void testInterface() {
    String interfaceName = "de.sormuras.compayler.CompaylerTest.Interface";

    Parser parser = new Parser(Interface.class);
    // TagFactory tagFactory = new QDoxTagFactory("src/test/" + interfaceName.replace('.', '/') + ".java");

    List<Descriptor> descriptors = parser.parse(interfaceName);

    Compayler.updateUniqueFlags(descriptors);
    Compayler.updateClassNames(descriptors);

    for (Descriptor descriptor : descriptors) {
      // System.out.println(descriptor);
      Source source = Compayler.createExecutableSource(interfaceName, descriptor);
      System.out.println(source.getCharContent(true));
    }

    Source decoratorSource = Compayler.createDecorator(interfaceName, "InterfaceDecorator", descriptors.toArray(new Descriptor[0]));
    System.out.println(decoratorSource.getCharContent(true));
  }

  // @Test
  // public void testGenerated() throws Exception {
  // Interface i = new Implementation();
  // Prevayler<Interface> p = PrevaylerFactory.createTransientPrevayler(i);
  // Interface d = new InterfaceDecorator(p);
  //
  // Assert.assertSame(d, d.direct());
  // Assert.assertNotEquals(0l, d.executionTime(new Date(0l)));
  // }

}
