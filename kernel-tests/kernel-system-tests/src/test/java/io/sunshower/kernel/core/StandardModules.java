package io.sunshower.kernel.core;

public enum StandardModules implements Installable {
  YAML("kernel-modules:sunshower-yaml-reader");
  final String path;

  StandardModules(String path) {
    this.path = path;
  }

  @Override
  public String getPath() {
    return path;
  }
}
