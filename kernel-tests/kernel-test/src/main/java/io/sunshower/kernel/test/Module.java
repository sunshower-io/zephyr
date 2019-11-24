package io.sunshower.kernel.test;

public @interface Module {

  String url() default "__NONE__";

  String project() default "__NONE__";

  Type type() default Type.Plugin;

  enum Type {
    Plugin,
    KernelModule
  }

  enum Lifecycle {
    /** Load module before suite is run. Module is loaded for all subsequent tests */
    LOAD_BEFORE_SUITE
  }
}
