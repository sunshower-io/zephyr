package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Version;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public final class ModuleCoordinate implements Coordinate {
  @NonNull private final String name;
  @NonNull private final String group;
  @NonNull private final Version version;

  public static Coordinate create(String group, String name, String version) {
    return new ModuleCoordinate(name, group, new SemanticVersion(version));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ModuleCoordinate)) return false;

    ModuleCoordinate that = (ModuleCoordinate) o;

    if (!getName().equals(that.getName())) return false;
    if (!getGroup().equals(that.getGroup())) return false;
    return getVersion().equals(that.getVersion());
  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getGroup().hashCode();
    result = 31 * result + getVersion().hashCode();
    return result;
  }

  @Override
  public int compareTo(@NotNull Coordinate o) {
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
}
