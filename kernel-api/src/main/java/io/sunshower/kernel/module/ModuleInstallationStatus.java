package io.sunshower.kernel.module;

import io.sunshower.kernel.Coordinate;
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
