package io.sunshower.kernel.test;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Modules.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {
  String NONE = "__NONE__";

  String url() default NONE;

  String project() default NONE;

  Type type() default Type.Plugin;

  String extension() default "war";

  enum Type {
    Plugin,
    KernelModule
  }

  enum Lifecycle {
    /** Load module before suite is run. Module is loaded for all subsequent tests */
    LOAD_BEFORE_SUITE
  }
}
