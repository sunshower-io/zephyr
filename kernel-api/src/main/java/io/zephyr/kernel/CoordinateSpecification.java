package io.zephyr.kernel;

import lombok.Getter;

/**
 * A coordinate represents a specific module, whereas a dependency coordinate represents a
 * collection of matching coordinates that may be selected later in the installation process.
 */
public class CoordinateSpecification {

  @Getter final String group;
  @Getter final String name;
  @Getter final String versionSpecification;

  public CoordinateSpecification(String group, String name, String versionSpecification) {
    this.group = group;
    this.name = name;
    this.versionSpecification = versionSpecification;
  }
}
