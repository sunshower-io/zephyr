package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.CoordinateSpecification;
import io.zephyr.kernel.Version;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

@Getter
@AllArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class ModuleCoordinate implements Coordinate {

  static final Pattern pattern = Pattern.compile(":");
  private final boolean resolved;
  @NonNull private final String name;
  @NonNull private final String group;
  @NonNull private final Version version;

  public ModuleCoordinate(final String name, final String group, final Version version) {
    this(name, group, version, true);
  }

  public ModuleCoordinate(
      final String name, final String group, final Version version, final boolean resolved) {
    this.name = name;
    this.group = group;
    this.version = version;
    this.resolved = resolved;
  }

  public static Coordinate create(String group, String name, String version) {
    return new ModuleCoordinate(name, group, new SemanticVersion(version));
  }

  public static Coordinate createUnresolvedCoordinate(CoordinateSpecification specification) {
    return new ModuleCoordinate(
        specification.getName(),
        specification.getGroup(),
        new SemanticVersion(specification.getVersionSpecification()),
        false);
  }

  public static Coordinate parse(@NonNull String name) {
    val segs = pattern.split(name);
    return create(segs[0], segs[1], segs[2]);
  }

  public static ModuleCoordinateQuery group(String group) {
    return new ModuleCoordinateQuery(group);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ModuleCoordinate)) {
      return false;
    }

    ModuleCoordinate that = (ModuleCoordinate) o;

    if (!getName().equals(that.getName())) {
      return false;
    }
    if (!getGroup().equals(that.getGroup())) {
      return false;
    }
    return getVersion().equals(that.getVersion());
  }

  @Override
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getGroup().hashCode();
    result = 31 * result + getVersion().hashCode();
    return result;
  }

  @Override
  public int compareTo(@NonNull Coordinate o) {
    val groupcmp = group.compareTo(o.getGroup());
    if (groupcmp != 0) {
      return groupcmp;
    }

    val namecmp = name.compareTo(o.getName());
    if (namecmp != 0) {
      return namecmp;
    }

    return version.compareTo(o.getVersion());
  }

  @Override
  public String toString() {
    return String.format("%s:%s:%s:%b", group, name, version, resolved);
  }

  @Override
  public boolean satisfies(String range) {
    return version.satisfies(range);
  }
}
