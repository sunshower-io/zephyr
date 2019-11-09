package io.sunshower.kernel.core;

import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("PMD.FinalizeOverloaded")
public class DefaultModuleManager implements ModuleManager {

  static final Logger log = Logging.get(DefaultModuleManager.class, "KernelMember");

  public DefaultModuleManager() {}

  @Override
  public ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group) {
    return null;
  }

  @Override
  public void initialize(Kernel kernel) {
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.initialize", new Object[] {this, kernel});
    }
    if (kernel == null) {
      throw new IllegalStateException("cannot initialize with null kernel");
    }
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.complete", new Object[] {this, kernel});
    }
  }

  @Override
  public void finalize(Kernel kernel) {}
}
