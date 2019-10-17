package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;

@Module
public class SunshowerKernelInjectionModule {

  @Provides
  public Kernel sunshowerKernel(SunshowerKernel kernel) {
    return kernel;
  }

  @Provides
  public PluginManager pluginManager() {
    return new PluginManager() {};
  }
}
