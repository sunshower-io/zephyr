package io.zephyr.kernel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * A coordinate represents a specific module, whereas a dependency coordinate represents a
 * collection of matching coordinates that may be selected later in the installation process.
 */
@ToString
@EqualsAndHashCode
public class CoordinateSpecification {

  @Getter final String group;
  @Getter final String name;
  @Getter final String versionSpecification;

  public CoordinateSpecification(
      @NonNull String group, @NonNull String name, @NonNull String versionSpecification) {
    this.group = group;
    this.name = name;
    this.versionSpecification = versionSpecification;
  }
}
