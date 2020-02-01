package io.sunshower.kernel.test;

import static org.mockito.Mockito.mock;

import io.sunshower.test.common.Tests;
import io.zephyr.api.ModuleActivator;
import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.cli.DefaultZephyr;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.*;
import io.zephyr.kernel.concurrency.ModuleThread;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.dependencies.DefaultDependencyGraph;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.service.KernelServiceRegistry;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KernelTestConfiguration {

  @Bean
  public ModuleContext moduleContext(Module module, Kernel kernel) {
    val ctx = new DefaultPluginContext(module, kernel);
    ((SimulatedModule) module).setContext(ctx);
    return ctx;
  }

  @Bean
  public ModuleActivator testPluginActivator() {
    return mock(ModuleActivator.class);
  }

  @Bean
  public Module testModule(ApplicationContext context) {
    return new SimulatedModule(Module.Type.Plugin, context);
  }

  @Bean
  public ModuleThread moduleThread(Module module, Kernel kernel) {
    val thread = new ModuleThread(module, kernel);
    ((SimulatedModule) module).setThread(thread);
    return thread;
  }

  @Bean
  public ModuleLifecycleManager moduleLifecycleManager(final Zephyr zephyr) {
    return new ModuleLifecycleManager(zephyr);
  }

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
  public Zephyr zephyr(Kernel kernel) {
    return new DefaultZephyr(kernel);
  }

  @Bean
  public DependencyGraph dependencyGraph() {
    return new DefaultDependencyGraph();
  }

  @Bean
  public ModuleClasspathManager moduleClasspathManager(DependencyGraph graph, Kernel kernel) {
    val result = new KernelModuleLoader(graph, kernel);
    ((SunshowerKernel) kernel).setModuleClasspathManager(result);
    return result;
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
    return new ExecutorWorkerPool(
        Executors.newFixedThreadPool(2),
        Executors.newCachedThreadPool(new NamedThreadFactory("kernel")));
  }

  @Bean
  public Scheduler<String> scheduler(WorkerPool pool) {
    return new KernelScheduler<>(pool);
  }

  @Bean
  public Kernel kernel(
      ModuleManager moduleManager, ServiceRegistry registry, Scheduler<String> scheduler) {
    val result = new SunshowerKernel(moduleManager, registry, scheduler);
    moduleManager.initialize(result);
    return result;
  }

  @Bean
  public ServiceRegistry serviceRegistry() {
    return new KernelServiceRegistry();
  }
}
