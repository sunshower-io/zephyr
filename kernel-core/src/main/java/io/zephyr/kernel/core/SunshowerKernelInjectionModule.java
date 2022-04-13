package io.zephyr.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.concurrency.KernelScheduler;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.dependencies.DefaultDependencyGraph;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.service.KernelServiceRegistry;
import javax.inject.Singleton;
import lombok.val;

@Module
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class SunshowerKernelInjectionModule {

  @Provides
  @Singleton
  public ServiceRegistry serviceRegistry() {
    return new KernelServiceRegistry();
  }

  @Provides
  @Singleton
  public Scheduler<String> kernelScheduler(WorkerPool pool) {
    return new KernelScheduler<>(pool);
  }

  @Provides
  @Singleton
  public DependencyGraph dependencyGraph() {
    return new DefaultDependencyGraph();
  }

  @Provides
  @Singleton
  public Kernel sunshowerKernel(
      ModuleManager moduleManager,
      DependencyGraph graph,
      KernelOptions options,
      ServiceRegistry registry,
      ClassLoader classLoader,
      Scheduler<String> scheduler) {
    SunshowerKernel.setKernelOptions(options);
    val kernel = new SunshowerKernel(moduleManager, registry, scheduler, classLoader);
    val classpathManager = Modules.moduleClasspathManager(graph, classLoader, kernel);
    kernel.setModuleClasspathManager(classpathManager);
    moduleManager.initialize(kernel);
    Framework.setInstance(kernel);
    return kernel;
  }

  @Provides
  @Singleton
  public ModuleManager pluginManager(DependencyGraph dependencyGraph) {
    return new DefaultModuleManager(dependencyGraph);
  }
}
