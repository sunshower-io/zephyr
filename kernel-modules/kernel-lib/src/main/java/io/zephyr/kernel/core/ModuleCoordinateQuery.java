package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Version;
import java.util.Objects;
import lombok.val;

@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class ModuleCoordinateQuery implements Coordinate {
  private String name;
  private String group;
  private Version version;

  ModuleCoordinateQuery(String group) {
    this.group = group;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public boolean satisfies(String range) {
    return version.satisfies(range);
  }

  @Override
  public int compareTo(Coordinate o) {
    if (o == null) {
      return 1;
    }

    val gcompare = compare(group, o.getGroup());
    if (gcompare != 0) {
      return gcompare;
    }

    val ncompare = compare(name, o.getName());
    if (ncompare != 0) {
      return ncompare;
    }

    val oversion = o.getVersion();
    if (oversion == null) {
      return 1;
    }

    if (version == null) {
      return -1;
    }

    return version.compareTo(oversion);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Coordinate)) return false;
    Coordinate that = (Coordinate) o;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getGroup(), that.getGroup())
        && Objects.equals(getVersion(), that.getVersion());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getGroup(), getVersion());
  }

  public ModuleCoordinateQuery version(String version) {
    this.version = new SemanticVersion(version);
    return this;
  }

  public ModuleCoordinateQuery name(String name) {
    this.name = name;
    return this;
  }

  /** lhs compare rhs lhs >= rhs */
  private static int compare(String lhs, String rhs) {
    if (lhs == null && rhs == null) {
      return 0;
    }

    if (lhs == null) {
      return -1;
    }
    return lhs.compareTo(rhs);
  }
}
