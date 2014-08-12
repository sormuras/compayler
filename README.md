# Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

Prevayler Decorator Compiler

**Java 8 required**


## Generated decorator class 
Use decorator over standard prevayler instance
	Prevayler prevayler = createPrevayler(new StringBuilder());
	Appendable appendable = new AppendableDecorator(prevayler);
	appendable.append('a').append("bc");
	prevayler.close();

### Annotation processor support
Add compayler.jar close to prevayler.jar and configure your build setup to execute annotation processors.
Then annotate your prevalent interface with `@Compayler.Decorate` and get the decorator class *for free*
and at compile time.

See http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html
or http://www.eclipse.org/jdt/apt/introToAPT.php
or http://www.jetbrains.com/idea/webhelp/annotation-processors-support.html
or https://netbeans.org/kb/docs/java/annotations.html

### Generate via command line
	java -jar compayler.jar java.lang.Appendable

## On-the-fly decoration with default Prevayler instance

	Compayler compayler = new Compayler(Appendable.class);
	Appendable appendable = compayler.decorate(new StringBuilder());
	
### On-the-fly with custom Prevayler instance, lambda-style

	Compayler compayler = new Compayler(Appendable.class);
	Appendable appendable = compayler.decorate(loader -> createPrevayler(new StringBuilder(), loader));
