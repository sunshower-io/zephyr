package io.zephyr.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.KernelScheduler;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.dependencies.DefaultDependencyGraph;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;

@Module
public class SunshowerKernelInjectionModule {

  @Provides
  @Singleton
  public WorkerPool workerPool() {
    // TODO make kernel thread pool configurable
    return new ExecutorWorkerPool(Executors.newFixedThreadPool(1));
  }

  @Provides
  @Singleton
  public Scheduler<String> kernelScheduler(WorkerPool pool) {
    return new KernelScheduler<>(pool);
  }

  @Provides
  @Singleton
  public ExecutorService executorService(KernelOptions options) {
    return Executors.newFixedThreadPool(options.getConcurrency());
  }

  @Provides
  @Singleton
  public DependencyGraph dependencyGraph() {
    return new DefaultDependencyGraph();
  }

  @Provides
  @Singleton
  public Kernel sunshowerKernel(SunshowerKernel kernel, ModuleManager moduleManager) {
    moduleManager.initialize(kernel);
    return kernel;
  }

  @Provides
  @Singleton
  public ModuleManager pluginManager(DependencyGraph dependencyGraph) {
    return new DefaultModuleManager(dependencyGraph);
  }

  @Provides
  @Singleton
  public ModuleClasspathManager moduleClasspathManager(
      DependencyGraph graph, ClassLoader classLoader) {
    return ServiceLoader.load(ModuleClasspathManagerProvider.class, classLoader)
        .findFirst()
        .get()
        .create(graph);
  }
}
