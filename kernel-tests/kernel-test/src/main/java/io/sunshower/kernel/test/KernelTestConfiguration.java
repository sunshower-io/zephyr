package io.sunshower.kernel.test;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.KernelScheduler;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.dependencies.DefaultDependencyGraph;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.inject.Inject;

@Configuration
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
