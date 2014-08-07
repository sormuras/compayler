package prevaylertutorial.e101;

import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.QUERY;

import org.prevayler.contrib.compayler.Compayler;

@Compayler.Decorate("demo/DecorationOfExample101")
public interface E101 {

  Person createPerson(String identity);

  Person deletePerson(String identity);

  @Compayler.Executable(QUERY)
  Person getPerson(String identity);

  @Compayler.Executable(QUERY)
  boolean isEmpty();

  void updatePersonName(String identity, String name);

}
