package org.prevayler.contrib.compayler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pass the execution time date instance instead of using a field value when calling the decorated implementation.
 * 
 * @author Christian Stein
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PrevalentDate {
  // empty
}
