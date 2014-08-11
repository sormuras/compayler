package prevaylertutorial.e101;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.prevayler.contrib.compayler.prevayler.PrevaylerFactory.prevayler;

import java.io.Closeable;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Prevayler;
import org.prevayler.contrib.compayler.Processor;
import org.prevayler.contrib.compayler.javac.Source;

public class E101Test {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  private E101 test(E101 e101) {
    Person person = e101.createPerson(UUID.randomUUID().toString());

    String nameOfPerson = "John Doe";
    e101.updatePersonName(person.getIdentity(), nameOfPerson);
    assertEquals(nameOfPerson, person.getName());

    Person queryResponse = e101.getPerson(person.getIdentity());
    assertSame("person and queryResponse are supposed to be the same object instance!", person, queryResponse);

    Person removed = e101.deletePerson(person.getIdentity());
    assertSame("person and removed are supposed to be the same object instance!", person, removed);

    assertTrue("There are not supposed to be any persons in the root at this point!", e101.isEmpty());
    return e101;
  }

  @Test
  public void testE101() throws Exception {
    test(new Root());
  }

  @Test
  public void testE101AsCompayled() throws Exception {
    File file = new File("src/test/prevaylertutorial/e101/E101.java");
    List<String> lines = Files.readAllLines(file.toPath());

    Source source = new Source(E101.class.getCanonicalName(), lines);
    ClassLoader loader = source.compile(new Processor());
    Prevayler<E101> prevayler = prevayler(new Root(), loader, temp.newFolder());

    @SuppressWarnings("unchecked")
    Class<? extends E101> decoratorClass = (Class<? extends E101>) loader.loadClass(E101.class.getCanonicalName() + "Decorator");

    E101 e101 = decoratorClass.getConstructor(Prevayler.class).newInstance(prevayler);
    test(e101);
    ((Closeable) e101).close();
  }

}
