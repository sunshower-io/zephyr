package io.zephyr.kernel.core;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import javax.inject.Singleton;

@Singleton
@Component(modules = SunshowerKernelInjectionModule.class)
public interface SunshowerKernelConfiguration {
  Kernel kernel();

  DependencyGraph dependencyGraph();

  @Component.Factory
  interface Builder {
    SunshowerKernelConfiguration create(
        @BindsInstance KernelOptions options, @BindsInstance ClassLoader bootstrapClassloader);
  }
}
