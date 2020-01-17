package io.zephyr.kernel.core;

import dagger.BindsInstance;
import dagger.Component;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import java.util.concurrent.Executors;
import javax.inject.Singleton;

@Singleton
@Component(modules = SunshowerKernelInjectionModule.class)
public interface SunshowerKernelConfiguration {
  Kernel kernel();

  DependencyGraph dependencyGraph();

  @Component.Factory
  interface Builder {
    SunshowerKernelConfiguration create(
        @BindsInstance KernelOptions options,
        @BindsInstance ClassLoader bootstrapClassloader,
        @BindsInstance WorkerPool workerPool);

    default SunshowerKernelConfiguration create(
        KernelOptions options, ClassLoader bootstrapClassloader) {
      return create(
          options,
          bootstrapClassloader,
          new ExecutorWorkerPool(Executors.newFixedThreadPool(1), Executors.newFixedThreadPool(1)));
    }
  }
}
