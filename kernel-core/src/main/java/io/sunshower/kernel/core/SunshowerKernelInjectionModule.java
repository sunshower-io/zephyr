package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.status.Status;

@Module
public class SunshowerKernelInjectionModule {

  @Provides
  public Kernel sunshowerKernel(SunshowerKernel kernel) {
    return kernel;
  }

  @Provides
  public ModuleManager pluginManager() {
    return new ModuleManager() {
      @Override
      public void addStatus(Status status) {}
    };
  }
}
