package io.zephyr.breeze;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reduction {

  /**
   * @return true if redundant edges should be eliminated
   */
  boolean coalesce() default true;


  /**
   * @return true if this task-graph should be parallelized
   */
  boolean parallel() default true;

  /**
   * @return the instantiator to use
   */
  Class<? extends Instantiator> instantiator() default Instantiator.class;


  String displayName() default Constants.DEFAULT_VALUE;
}
