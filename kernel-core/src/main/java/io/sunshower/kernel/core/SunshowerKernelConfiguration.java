package io.sunshower.kernel.core;

import dagger.Component;
import io.sunshower.kernel.dependencies.DependencyGraph;
import javax.inject.Singleton;

@Singleton
@Component(modules = SunshowerKernelInjectionModule.class)
public interface SunshowerKernelConfiguration {
  Kernel kernel();

  DependencyGraph dependencyGraph();
}
