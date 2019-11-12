package io.sunshower.kernel.core;

import io.sunshower.kernel.classloading.KernelClassloader;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.core.lifecycle.DefaultKernelLifecycle;
import io.sunshower.kernel.launch.KernelOptions;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

@SuppressWarnings({"PMD.AvoidUsingVolatile", "PMD.DoNotUseThreads"})
public class SunshowerKernel implements Kernel {

  private final Scheduler<String> scheduler;
  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  private final ModuleClasspathManager moduleClasspathManager;

  /** Instance fields */
  private volatile ClassLoader classLoader;

  @Getter @Setter private volatile FileSystem fileSystem;

  @Getter private final ModuleManager moduleManager;

  private final KernelLifecycle lifecycle;

  @Inject
  public SunshowerKernel(
      ModuleClasspathManager moduleClasspathManager,
      ModuleManager moduleManager,
      Scheduler<String> scheduler) {
    this.scheduler = scheduler;
    this.moduleManager = moduleManager;
    this.moduleClasspathManager = moduleClasspathManager;
    this.lifecycle = new DefaultKernelLifecycle(this, scheduler);
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }

  @Override
  public KernelLifecycle getLifecycle() {
    return lifecycle;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public <T> List<T> locateServices(Class<T> type) {
    val result = new ArrayList<T>();
    load(result, type, getClassLoader());

    return result;
  }

  @Override
  @SneakyThrows
  public void start() {
    lifecycle.start().toCompletableFuture().get();
  }

  @Override
  public void reload() {
    stop();
    start();
  }

  @Override
  @SneakyThrows
  public void stop() {
    lifecycle.stop().toCompletableFuture().get();
  }

  @Override
  public ModuleClasspathManager getModuleClasspathManager() {
    return moduleClasspathManager;
  }

  @Override
  public Scheduler<String> getScheduler() {
    return scheduler;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private <T> void load(List<T> result, Class<T> type, ClassLoader classLoader) {
    val loader = ServiceLoader.load(type, classLoader);
    for (val service : loader) {
      result.add(service);
    }
  }

  public void setClassLoader(KernelClassloader loader) {
    this.classLoader = loader;
  }
}
