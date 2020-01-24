package io.zephyr.kernel.module;

import io.zephyr.kernel.Coordinate;
import java.util.List;

public class ModuleInstallationStatus {

  final ModuleInstallationRequest request;

  public ModuleInstallationStatus(ModuleInstallationRequest request) {
    this.request = request;
  }

  public List<Coordinate> getUnsatisifedDependencies() {
    return null;
  }
}
