## Compayler [![Build Status](https://travis-ci.org/sormuras/compayler.png?branch=master)](https://travis-ci.org/sormuras/compayler)

Prevayler Decorator Compiler

# Generate decorator source 

  Appendable appendable = new AppendableDecorator(new StringBuilder());


# On-the-fly decoration

  Appendable appendable = new Compayler(Appendable.class).decorate(new StringBuilder());
