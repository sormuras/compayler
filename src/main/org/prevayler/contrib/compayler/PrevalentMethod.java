package org.prevayler.contrib.compayler;

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
   * Direct invocation on the prevalent system, bypassing the Prevayler.
   * 
   * No transaction is journaled for system recovery. No synchronization performed by Prevayler.
   */
  boolean direct() default false;

  /**
   * Query or transaction mode?
   */
  PrevalentType value() default PrevalentType.TRANSACTION;

}