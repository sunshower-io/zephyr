package io.sunshower.kernel.core;

import dagger.Component;

@Component(modules = SunshowerKernelTestModule.class)
public interface SunshowerKernelTests {
  Kernel kernel();
}
