package io.zephyr.kernel;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class KernelModuleEntry {
  private final int order;
  private final String name;
  private final String group;
  private final String version;
  private final List<String> libraryFiles;

  public static final String MODULE_LIST = "modules.list";

  @Override
  public String toString() {
    if (libraryFiles.isEmpty()) {
      return String.format("%d:%s:%s:%s", order, group, name, version);
    } else {
      return String.format(
          "%d:%s:%s:%s[%s]", order, group, name, version, String.join(",", libraryFiles));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof KernelModuleEntry)) return false;

    KernelModuleEntry that = (KernelModuleEntry) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (group != null ? !group.equals(that.group) : that.group != null) return false;
    return version != null ? version.equals(that.version) : that.version == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (group != null ? group.hashCode() : 0);
    result = 31 * result + (version != null ? version.hashCode() : 0);
    return result;
  }
}
