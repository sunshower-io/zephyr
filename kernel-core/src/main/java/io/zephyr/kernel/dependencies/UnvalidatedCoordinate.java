package io.zephyr.kernel.dependencies;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.CoordinateSpecification;
import io.zephyr.kernel.Version;
import io.zephyr.kernel.core.StringVersion;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
final class UnvalidatedCoordinate implements Coordinate {

  @Getter private final String name;
  @Getter private final String group;
  private final StringVersion version;

  UnvalidatedCoordinate(String group, String name, String version) {
    this.name = name;
    this.group = group;
    this.version = new StringVersion(version);
  }

  public UnvalidatedCoordinate(CoordinateSpecification coordinate) {
    this(coordinate.getGroup(), coordinate.getName(), coordinate.getVersionSpecification());
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public boolean satisfies(String range) {
    throw new UnsupportedOperationException("Not a real coordinate");
  }

  @Override
  public int compareTo(Coordinate coordinate) {
    throw new UnsupportedOperationException("not a real coordinate");
  }

  public String toString() {
    return String.format("group=%s:name=%s:version=%s", group, name, version);
  }
}
