package io.zephyr.spring.embedded;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.cli.DefaultZephyr;
import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.*;
import io.zephyr.kernel.core.*;
import io.zephyr.kernel.dependencies.DefaultDependencyGraph;
import io.zephyr.kernel.dependencies.DependencyGraph;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.memento.Memento;
import io.zephyr.kernel.service.KernelServiceRegistry;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class EmbeddedSpringConfiguration {

  @Bean
  public ModuleContext moduleContext(Module module, Kernel kernel, ModuleThread thread) {
    val ctx = new DefaultPluginContext(module, kernel, thread);
    ((EmbeddedModule) module).setContext(ctx);
    return ctx;
  }

  @Bean
  @DependsOn("kernelOptions")
  public Module embeddedModule(
      ApplicationContext context,
      Memento memento,
      ModuleDescriptor descriptor,
      FileSystem fileSystem,
      ModuleClasspath classpath) {
    return new EmbeddedModule(
        Module.Type.Plugin, context, memento, classpath, fileSystem, descriptor);
  }

  @Bean
  public FileSystem moduleFileSystem(ModuleDescriptor descriptor, Kernel kernel)
      throws IOException {
    return Modules.getFileSystem(descriptor.getCoordinate(), kernel).snd;
  }

  @Bean
  @DependsOn("kernelOptions")
  public ModuleThread moduleThread(Module module, Kernel kernel) {
    val thread = new ModuleThread(module, kernel);
    ((EmbeddedModule) module).setThread(thread);
    return thread;
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
        Executors.newCachedThreadPool(new NamedThreadFactory("zephyr")),
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
    Framework.setInstance(result);
    return result;
  }

  @Bean
  public ServiceRegistry serviceRegistry() {
    return new KernelServiceRegistry();
  }
}
