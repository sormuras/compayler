package prevaylertutorial.e101;

import org.prevayler.contrib.compayler.PrevalentMethod;
import org.prevayler.contrib.compayler.PrevalentType;

public interface E101 {

  Person createPerson(String identity);

  Person deletePerson(String identity);

  @PrevalentMethod(PrevalentType.QUERY)
  Person getPerson(String identity);

  @PrevalentMethod(PrevalentType.QUERY)
  boolean isEmpty();

  void updatePersonName(String identity, String name);

}
