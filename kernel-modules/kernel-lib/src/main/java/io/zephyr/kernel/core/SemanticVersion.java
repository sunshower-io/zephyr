package io.zephyr.kernel.core;

import com.vdurmont.semver4j.Semver;
import io.zephyr.kernel.Version;
import lombok.NonNull;

public class SemanticVersion implements Version {

  private final Semver version;

  public SemanticVersion(@NonNull String spec) {
    version = new Semver(spec);
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (getClass().equals(o.getClass())) {
      return version.equals(((SemanticVersion) o).version);
    }
    return false;
  }

  @Override
  public String toString() {
    return version.toString();
  }

  @Override
  public int compareTo(@NonNull Version o) {
    if (o.getClass().equals(SemanticVersion.class)) {
      return version.compareTo(((SemanticVersion) o).version);
    }
    throw new IllegalArgumentException("Can't compare myself to that");
  }

  @Override
  public boolean satisfies(String range) {
    return version.satisfies(range);
  }
}
