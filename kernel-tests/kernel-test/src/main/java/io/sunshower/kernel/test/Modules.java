package io.sunshower.kernel.test;

public @interface Modules {
  Module[] value() default {};
}
