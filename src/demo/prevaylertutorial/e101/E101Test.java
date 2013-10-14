package prevaylertutorial.e101;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.prevayler.Query;
import org.prevayler.contrib.compayler.Compayler;
import org.prevayler.contrib.compayler.PrevaylerCreator;
import org.prevayler.contrib.compayler.PrevaylerDecorator;

public class E101Test {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void test() throws Exception {

    String prevalenceBase = temporaryFolder.newFolder("PrevalenceBase_" + System.currentTimeMillis()).toString();
    PrevaylerCreator<E101> creator = new PrevaylerCreator.DefaultPrevaylerCreator<E101>(new Root(), prevalenceBase);

    try (PrevaylerDecorator<E101> decorator = new Compayler<>(E101.class).toDecorator(creator)) {
      E101 e101 = (E101) decorator;

      Person person = e101.createPerson(UUID.randomUUID().toString());

      String nameOfPerson = "John Doe";

      e101.updatePersonName(person.getIdentity(), nameOfPerson);
      assertEquals(nameOfPerson, person.getName());

      Person queryResponse = e101.getPerson(person.getIdentity());
      assertSame("person and queryResponse are supposed to be the same object instance!", person, queryResponse);

      Person removed = e101.deletePerson(person.getIdentity());
      assertSame("person and removed are supposed to be the same object instance!", person, removed);

      assertTrue("There are not supposed to be any persons in the root at this point!",
          decorator.prevayler().execute(new Query<E101, Boolean>() {
            private static final long serialVersionUID = 1L;

            public Boolean query(E101 prevalentSystem, Date executionTime) throws Exception {
              return prevalentSystem.isEmpty();
            }
          }));

    }

  }

}
