package io.sunshower.kernel.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleInstallationGroup {
  final List<ModuleInstallationRequest> modules;

  public ModuleInstallationGroup(ModuleInstallationRequest... modules) {
    this.modules = new ArrayList<>(modules.length);
    if (modules.length > 0) {
      this.modules.addAll(Arrays.asList(modules));
    }
  }

  public ModuleInstallationGroup add(ModuleInstallationRequest request) {
    modules.add(request);
    return this;
  }
}
