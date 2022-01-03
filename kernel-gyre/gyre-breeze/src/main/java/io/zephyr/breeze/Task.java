package io.zephyr.breeze;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** define a task declaratively */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {

  /** @return the displayName of this task. Defaults to name() if present or the typename */
  String displayName() default Constants.DEFAULT_VALUE;
  /** @return the name of this task */
  String value() default Constants.DEFAULT_VALUE;

  /**
   * alias for @value
   *
   * @return the name of this task
   */
  String name() default Constants.DEFAULT_VALUE;

  /**
   * the instantiator to instantiate instances of the annotated type with
   *
   * @return the type of the instantiator
   */
  Class<? extends Instantiator> instantiator() default Instantiator.class;
}
