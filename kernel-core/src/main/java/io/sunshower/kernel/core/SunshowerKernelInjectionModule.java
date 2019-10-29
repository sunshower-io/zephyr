package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.ServiceLoader;
import javax.inject.Singleton;
import lombok.NonNull;

@Module
public class SunshowerKernelInjectionModule {

  private final KernelOptions options;
  private final ClassLoader classLoader;

  public SunshowerKernelInjectionModule(
      @NonNull final KernelOptions options, @NonNull final ClassLoader kernelClassLoader) {
    this.options = options;
    this.classLoader = kernelClassLoader;
  }

  @Provides
  @Singleton
  public DependencyGraph dependencyGraph() {
    return new DefaultDependencyGraph();
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
  public ModuleManager pluginManager(
      DefaultModuleContext context,
      ModuleClasspathManager classpathManager,
      DependencyGraph dependencyGraph) {
    return new DefaultModuleManager(context, classpathManager, dependencyGraph);
  }

  @Provides
  public ModuleClasspathManager moduleClasspathManager(DependencyGraph graph) {
    return ServiceLoader.load(ModuleClasspathManagerProvider.class, classLoader)
        .findFirst()
        .get()
        .create(graph);
  }
}
