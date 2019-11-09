package io.sunshower.kernel.core;

import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;

public class DefaultModuleManager implements ModuleManager {

  @Override
  public ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group) {
    return null;
  }
}
