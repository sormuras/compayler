# Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

***Prevayler Decorator Compiler (Java 8)***

With the Prevayler Decorator Compiler you can achive these goals

* Encapsulate all transactions executing on a prevalent system in one place, namely an interface.

* Easily unit test the system implementation without caring for persistence.

* Let the compiler do the tedious work of writing the transaction source code.

* Never instantiate a transaction object by yourself, just call your interface methods.

* Review/tweak the generated decorator source code.

* Less overhead compared to a runtime/reflection based solution

## Generate decorator class via annotation processor

Add compayler-X.Y.jar close to prevayler.jar and configure your build setup to execute annotation
processors. Then annotate your prevalent interface with `@Compayler.Decorate` and get the decorator class
with all serializable transaction and query classes *for free* and ahead of compile time.

See documentation for [javac](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html)
or [Eclipse](http://www.eclipse.org/jdt/apt/introToAPT.php)
or [Idea](http://www.jetbrains.com/idea/webhelp/annotation-processors-support.html)
or [Netbeans](https://netbeans.org/kb/docs/java/annotations.html) or your favorite IDE

### Example based on [E101](https://github.com/jsampson/prevayler/tree/master/demos/tutorial/src/test/java/org/prevayler/examples/e101)
* Create interface `Root` and annotate it
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
* Implement the `Root` interface with your business logic in `RootSystem`. Here, you can unit test the system
without caring for persistence because there is no reference to Prevayler classes.
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
* Finally, use generated `RootDecorator` over `Prevayler` over `RootSystem`
```java
    Prevayler prevayler = createPrevayler(new RootSystem(), new File("e101"));
    try (Root root = new RootDecorator(prevayler)) {
      Person person = root.createPerson(UUID.randomUUID().toString());
      String nameOfPerson = "John Doe";
      root.updatePersonName(person.getIdentity(), nameOfPerson);
      assertEquals(nameOfPerson, person.getName());
      Person queryResponse = root.getPerson(person.getIdentity());
      assertSame("person and response not same object?!", person, queryResponse);
      Person removed = root.deletePerson(person.getIdentity());
      assertSame("person and removed not same object?!", person, removed);
      assertTrue("root not empty?!", root.isEmpty());
    }
```
