package io.zephyr.kernel.core;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import lombok.NonNull;

public interface ModuleClasspathManager {

  void install(@NonNull Module module);

  void uninstall(@NonNull Coordinate coordinate);

  void uninstall(@NonNull Module module);

  void check(Module module);
}
