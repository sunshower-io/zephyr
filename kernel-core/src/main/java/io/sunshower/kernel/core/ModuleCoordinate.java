package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Version;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ModuleCoordinate implements Coordinate {
  @NonNull private final String name;
  @NonNull private final String group;
  @NonNull private final Version version;

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
