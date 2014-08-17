# Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

***Prevayler Decorator Compiler (Java 8)***

With Compayler for Prevayler you can achive some major goals

1. Encapsulate all transactions executing on a prevalent system in one place, namely an interface.

2. Easily unit test the system implementation without caring for persistence.

3. Let the compiler do the tedious work of writing the transaction source code.

## Generate decorator class via annotation processor

First, add compayler-X.Y.jar close to prevayler.jar and configure your build setup to execute annotation
processors. Then annotate your prevalent interface with `@Compayler.Decorate` and get the decorator class
with all serializable transaction and query classes *for free* and at compile time.

See documentation for [javac](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html)
or [Eclipse](http://www.eclipse.org/jdt/apt/introToAPT.php)
or [Idea](http://www.jetbrains.com/idea/webhelp/annotation-processors-support.html)
or [Netbeans](https://netbeans.org/kb/docs/java/annotations.html) or your favorite IDE

### Example based on [E101](https://github.com/jsampson/prevayler/tree/master/demos/tutorial/src/test/java/org/prevayler/examples/e101)
1. Create interface `Root` and annotate it
```java
@Decorate
interface Root extends Closeable, Serializable {
  Person createPerson(String identity);
  Person deletePerson(String identity);
  @Execute(QUERY) Person getPerson(String identity);
  @Execute(DIRECT) boolean isEmpty();
  void updatePersonName(String identity, String name);
}
```
2. Implement the Root interface with your business logic in `RootSystem`. Here, you can unit test the system
without caring for persistence because there is reference to Prevayler classes.
```java
class RootSystem implements Root {
  private static final long serialVersionUID = 170l;
  private Map<String, Person> persons = new HashMap<>();
  @Override
  public Person createPerson(String identity) {
    Person entity = new Person();
    entity.setIdentity(identity);
    persons.put(entity.getIdentity(), entity);
    return entity;
  }
  ...
}
```
3. Finally, use generated `RootDecorator` over prevaylent system `RootSystem`
```java
    Prevayler prevayler = createPrevayler(new RootSystem(), new File("e101"));
    try (Root root = new RootDecorator(prevayler)) {
      Person person = root.createPerson(UUID.randomUUID().toString());
      String nameOfPerson = "John Doe";
      root.updatePersonName(person.getIdentity(), nameOfPerson);
      assertEquals(nameOfPerson, person.getName());
      Person queryResponse = root.getPerson(person.getIdentity());
      assertSame("person and queryResponse are supposed to be the same object instance!", person, queryResponse);
      Person removed = root.deletePerson(person.getIdentity());
      assertSame("person and removed are supposed to be the same object instance!", person, removed);
      assertTrue("there are not supposed to be any persons in the root at this point!", root.isEmpty());
    }
```
