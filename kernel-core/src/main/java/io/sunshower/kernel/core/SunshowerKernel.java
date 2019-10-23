package io.sunshower.kernel.core;

import io.sunshower.kernel.launch.KernelOptions;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;

public class SunshowerKernel implements Kernel {

  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  /** Instance fields */
  @Getter private final ModuleManager moduleManager;

  @Inject
  public SunshowerKernel(ModuleManager moduleManager) {
    this.moduleManager = moduleManager;
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }
}
