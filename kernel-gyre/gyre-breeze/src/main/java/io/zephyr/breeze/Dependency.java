package io.zephyr.breeze;

import io.zephyr.breeze.Dependency.Dependencies;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Repeatable(Dependencies.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {


  /**
   * if a type-value is not present, then attempt to resolve
   * this dependency by name
   * @return the name of the dependency
   */
  String value() default Constants.DEFAULT_VALUE;
  /**
   * @return the type of the dependency
   */
  Class<?> type() default Class.class;


  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @interface Dependencies {

    Dependency[] value();

  }
}
