package com.github.sormuras.compayler;

import static com.github.sormuras.compayler.PrevalentMode.PREVALENT;
import static com.github.sormuras.compayler.PrevalentType.TRANSACTION;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods processed by the Compayler.
 * 
 * @author Christian Stein
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PrevalentMethod {

  /**
   * Direct invocation on the prevalent system, bypassing the Prevayler framework.
   * 
   * No transaction is journaled for system recovery. No synchronization performed by Prevayler.
   */
  PrevalentMode mode() default PREVALENT;

  /**
   * Index of the execution time parameter. The parameter to which the index points to, must be of type <code>java.util.Date</code>.
   */
  int time() default -1;

  /**
   * Query or transaction type?
   */
  PrevalentType value() default TRANSACTION;

}
