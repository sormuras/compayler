package de.sormuras.compayler;

public interface Apis {

  interface Nested {

    interface Deeply extends Appendable {

      Deeply variable(Object... ignore);
      
      Appendable variable(Object[][] objects, Object[]... ignore);

    }

  }

}
