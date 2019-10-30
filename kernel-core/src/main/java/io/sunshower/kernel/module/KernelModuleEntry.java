package io.sunshower.kernel.module;

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
}
