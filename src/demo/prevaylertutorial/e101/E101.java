package prevaylertutorial.e101;

public interface E101 {

  Person createPerson(String identity);

  Person deletePerson(String identity);

  // @Compayler.Directive(Mode.QUERY)
  Person getPerson(String identity);

  // @Compayler.Directive(Mode.DIRECT)
  boolean isEmpty();

  void updatePersonName(String identity, String name);

}
