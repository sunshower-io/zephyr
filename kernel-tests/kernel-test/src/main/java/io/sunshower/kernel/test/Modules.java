package io.sunshower.kernel.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Modules {
  Module[] value() default {};
}
