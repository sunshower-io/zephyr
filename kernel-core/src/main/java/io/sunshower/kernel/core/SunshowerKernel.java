package io.sunshower.kernel.core;

import io.sunshower.kernel.launch.KernelOptions;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;

public class SunshowerKernel implements Kernel {

  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  /** Instance fields */
  @Getter private final PluginManager pluginManager;

  @Inject
  public SunshowerKernel(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }
}
