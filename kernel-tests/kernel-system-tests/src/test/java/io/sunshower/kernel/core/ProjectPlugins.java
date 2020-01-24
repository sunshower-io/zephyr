package io.sunshower.kernel.core;

public enum ProjectPlugins implements Installable {
  TEST_PLUGIN_1("kernel-tests:test-plugins:test-plugin-1"),
  TEST_PLUGIN_2("kernel-tests:test-plugins:test-plugin-2"),
  TEST_PLUGIN_3("kernel-tests:test-plugins:test-plugin-3"),
  TEST_PLUGIN_SPRING("kernel-tests:test-plugins:test-plugin-spring"),
  TEST_PLUGIN_SPRING_DEP("kernel-tests:test-plugins:test-plugin-spring-dep");

  final String path;

  ProjectPlugins(String path) {
    this.path = path;
  }

  @Override
  public String getPath() {
    return path;
  }
}
