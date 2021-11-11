package io.sunshower.kernel.core;

public enum ProjectPlugins implements Installable {
  TEST_PLUGIN_1("kernel-tests:test-plugins:test-plugin-1"),
  TEST_PLUGIN_2("kernel-tests:test-plugins:test-plugin-2"),
  TEST_PLUGIN_3("kernel-tests:test-plugins:test-plugin-3"),
  TEST_PLUGIN_SPRING("kernel-tests:test-plugins:test-plugin-spring"),
  TEST_PLUGIN_SPRING_DEP("kernel-tests:test-plugins:test-plugin-spring-dep"),

  MODULE_ONE_V1("kernel-tests:test-plugins:module-order:module-one-v1"),
  MODULE_ONE_V2("kernel-tests:test-plugins:module-order:module-one-v2"),
  DEPENDENT_MODULE("kernel-tests:test-plugins:module-order:dependent-module");

  final String path;

  ProjectPlugins(String path) {
    this.path = path;
  }

  @Override
  public String getPath() {
    return path;
  }
}
