package prevaylertutorial.e101;

import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.DIRECT;
import static org.prevayler.contrib.compayler.Compayler.ExecutionMode.QUERY;

import org.prevayler.contrib.compayler.Compayler.Decorate;
import org.prevayler.contrib.compayler.Compayler.Execute;

@Decorate("demo/DecorationOfExample101")
public interface E101 {

  Person createPerson(String identity);

  Person deletePerson(String identity);

  @Execute(QUERY)
  Person getPerson(String identity);

  @Execute(DIRECT)
  boolean isEmpty();

  void updatePersonName(String identity, String name);

}
