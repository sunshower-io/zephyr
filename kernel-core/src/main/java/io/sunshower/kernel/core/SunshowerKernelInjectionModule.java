package io.sunshower.kernel.core;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.concurrency.ExecutorWorkerPool;
import io.sunshower.kernel.concurrency.KernelScheduler;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.concurrency.WorkerPool;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  @Singleton
  @Provides
  public KernelOptions kernelOptions() {
    return options;
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
  public ModuleClasspathManager moduleClasspathManager(DependencyGraph graph) {
    return ServiceLoader.load(ModuleClasspathManagerProvider.class, classLoader)
        .findFirst()
        .get()
        .create(graph);
  }
}
