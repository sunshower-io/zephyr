package io.sunshower.kernel.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Clean {
  Mode value() default Mode.Before;

  Mode mode() default Mode.Before;

  Context context() default Context.Method;

  enum Context {
    Class,
    Method
  }

  enum Mode {
    Before,
    After
  }
}
