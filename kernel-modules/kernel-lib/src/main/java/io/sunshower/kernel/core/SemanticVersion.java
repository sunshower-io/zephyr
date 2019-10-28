package io.sunshower.kernel.core;

import io.sunshower.kernel.Version;
import lombok.NonNull;

public class SemanticVersion implements Version {

  private final com.github.zafarkhaja.semver.Version version;

  public SemanticVersion(String spec) {
    version = com.github.zafarkhaja.semver.Version.valueOf(spec);
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
}
