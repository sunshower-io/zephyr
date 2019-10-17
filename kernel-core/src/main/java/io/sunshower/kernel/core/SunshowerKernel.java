package io.sunshower.kernel.core;

import javax.inject.Inject;
import lombok.Getter;

public class SunshowerKernel implements Kernel {
  @Getter private final PluginManager pluginManager;

  @Inject
  public SunshowerKernel(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }
}
