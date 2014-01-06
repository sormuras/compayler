package prevaylertutorial.e101;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class E101Test {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Test
  public void testE101() throws Exception {
    test(new Root());
    // Compayler<E101, Root> compayler = new Compayler<>(E101.class, Root.class);
    // try (Decorator<E101, Root> decorator = compayler.decorate(new Root(), temp.newFolder())) {
    // E101 e101 = decorator.asPrevalentInterface();
    // }
  }

  private void test(E101 e101) {
    Person person = e101.createPerson(UUID.randomUUID().toString());
    String nameOfPerson = "John Doe";
    e101.updatePersonName(person.getIdentity(), nameOfPerson);
    assertEquals(nameOfPerson, person.getName());
    Person queryResponse = e101.getPerson(person.getIdentity());
    assertSame("person and queryResponse are supposed to be the same object instance!", person, queryResponse);
    Person removed = e101.deletePerson(person.getIdentity());
    assertSame("person and removed are supposed to be the same object instance!", person, removed);
    assertTrue("There are not supposed to be any persons in the root at this point!", e101.isEmpty());
  }

}
