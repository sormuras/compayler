# Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

Prevayler Decorator Compiler

## Generated decorator source 

### Generate "AppendableDecorator"
	java -jar compayler java.lang.Appendable

### Use decorator over standard prevayler instance
	Prevayler prevayler = createPrevayler(new StringBuilder());
	Appendable appendable = new AppendableDecorator(prevayler);


## On-the-fly decoration

	Compayler compayler = new Compayler(Appendable.class);
	Appendable appendable = compayler.decorate(new StringBuilder());
	
### On-the-fly with custom Prevayler instance, lambda-style

	Compayler compayler = new Compayler(Appendable.class);
	Appendable appendable = compayler.decorate(loader -> createPrevayler(new StringBuilder(), loader));
