package prevaylertutorial.e101;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Root implements Serializable, E101 {

  private static final long serialVersionUID = 1l;

  private Map<String, Person> persons = new HashMap<>();

  @Override
  public void close() throws IOException {
    // no-op
  }

  @Override
  public Person createPerson(String identity) {
    Person entity = new Person();
    entity.setIdentity(identity);
    persons.put(entity.getIdentity(), entity);
    return entity;
  }

  @Override
  public Person deletePerson(String identity) {
    return persons.remove(identity);
  }

  @Override
  public Person getPerson(String identity) {
    return persons.get(identity);
  }

  @Override
  public boolean isEmpty() {
    return persons.isEmpty();
  }

  @Override
  public void updatePersonName(String identity, String name) {
    persons.get(identity).setName(name);
  }

}
