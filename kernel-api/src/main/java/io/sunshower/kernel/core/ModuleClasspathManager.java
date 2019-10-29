package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Module;
import lombok.NonNull;

public interface ModuleClasspathManager {

  void install(@NonNull Module module);

  void uninstall(@NonNull Coordinate coordinate);

  void uninstall(@NonNull Module module);
}
