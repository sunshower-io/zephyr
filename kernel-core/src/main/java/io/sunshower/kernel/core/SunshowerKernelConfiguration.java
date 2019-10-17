package io.sunshower.kernel.core;

import dagger.Component;

@Component(modules = SunshowerKernelInjectionModule.class)
public interface SunshowerKernelConfiguration {
  Kernel kernel();
}
