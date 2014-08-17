# Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

Prevayler Decorator Compiler (Java 8)

## Generate decorator class via annotation processor

Add compayler.jar close to prevayler.jar and configure your build setup to execute annotation processors.
Then annotate your prevalent interface with `@Compayler.Decorate` and get the decorator class *for free*
and at compile time.

See documentation for [javac](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html)
or [Eclipse](http://www.eclipse.org/jdt/apt/introToAPT.php)
or [Idea](http://www.jetbrains.com/idea/webhelp/annotation-processors-support.html)
or [Netbeans](https://netbeans.org/kb/docs/java/annotations.html)...

### Example based on [E101](https://github.com/jsampson/prevayler/tree/master/demos/tutorial/src/test/java/org/prevayler/examples/e101)

    @Decorate
    interface Root extends Closeable {
      Person createPerson(String identity);
      Person deletePerson(String identity);
      @Execute(QUERY) Person getPerson(String identity);
      @Execute(DIRECT) boolean isEmpty();
      void updatePersonName(String identity, String name);
    }

Use generated decorator over prevaylent system instance, here RootSystem

    try (Root root = new RootDecorator(PrevaylerFactory.createPrevayler(new RootSystem(), new File("e101")))) {
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
