package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;

@Module
public class SunshowerKernelTestModule {
  @Provides
  public Kernel kernel() {
    return new SunshowerKernel();
  }

  @Provides
  public String whatever(Kernel kernel) {
    return kernel.toString();
  }
}
