package io.zephyr.kernel.core;

import io.zephyr.kernel.Version;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class StringVersion implements Version {

  private final String value;

  public StringVersion(@NonNull String value) {
    this.value = value;
  }

  @Override
  public boolean satisfies(String range) {
    return value.equals(range);
  }

  @Override
  public int compareTo(Version version) {
    return value.compareTo(version.toString());
  }

  @Override
  public String toString() {
    return value;
  }
}
