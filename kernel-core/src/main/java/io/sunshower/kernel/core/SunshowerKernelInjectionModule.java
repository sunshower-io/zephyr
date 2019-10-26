package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.launch.KernelOptions;
import lombok.NonNull;

@Module
public class SunshowerKernelInjectionModule {

  private final KernelOptions options;

  public SunshowerKernelInjectionModule(@NonNull final KernelOptions options) {
    this.options = options;
  }

  @Provides
  public KernelOptions kernelOptions() {
    return options;
  }

  @Provides
  public DefaultModuleContext moduleContext() {
    return new DefaultModuleContext();
  }

  @Provides
  public Kernel sunshowerKernel(SunshowerKernel kernel) {
    return kernel;
  }

  @Provides
  public ModuleManager pluginManager(DefaultModuleContext context) {
    return new DefaultModuleManager(context);
  }
}
