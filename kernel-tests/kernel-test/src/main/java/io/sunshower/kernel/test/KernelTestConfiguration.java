package io.sunshower.kernel.test;

import io.sunshower.kernel.concurrency.ExecutorWorkerPool;
import io.sunshower.kernel.concurrency.KernelScheduler;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.concurrency.WorkerPool;
import io.sunshower.kernel.core.*;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class KernelTestConfiguration {

  @Bean
  public File kernelRootDirectory() {
    return Tests.createTemp();
  }

  @Bean
  public KernelOptions kernelOptions(File kernelRootDirectory) {
    val options = new KernelOptions();
    options.setHomeDirectory(kernelRootDirectory);
    SunshowerKernel.setKernelOptions(options);
    return options;
  }

  @Bean
  public DependencyGraph dependencyGraph() {
    return new DefaultDependencyGraph();
  }

  @Bean
  public ModuleClasspathManager moduleClasspathManager(DependencyGraph graph) {
    return new KernelModuleLoader(graph);
  }

  @Bean
  public ModuleManager moduleManager(DependencyGraph dependencyGraph) {
    return new DefaultModuleManager(dependencyGraph);
    //    return new DefaultModuleManager(moduleContext, moduleClasspathManager, dependencyGraph);
  }

  @Bean
  public KernelLifecycle kernelLifecycle(Kernel kernel) {
    return kernel.getLifecycle();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  @Bean
  public WorkerPool workerPool() {
    return new ExecutorWorkerPool(Executors.newFixedThreadPool(2));
  }

  @Bean
  public Scheduler<String> scheduler(WorkerPool pool) {
    return new KernelScheduler<>(pool);
  }

  @Bean
  public Kernel kernel(
      ModuleManager moduleManager,
      Scheduler scheduler,
      ModuleClasspathManager moduleClasspathManager) {

    val result = new SunshowerKernel(moduleClasspathManager, moduleManager, scheduler);
    moduleManager.initialize(result);
    return result;
  }
}
