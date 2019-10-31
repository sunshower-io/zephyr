package io.sunshower.kernel.core;

import io.sunshower.kernel.classloading.KernelClassloader;
import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.module.ModuleEntryWriteProcessor;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@SuppressWarnings({"PMD.AvoidUsingVolatile", "PMD.DoNotUseThreads"})
public class SunshowerKernel implements Kernel {

  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  /** Instance fields */
  private volatile ClassLoader classLoader;

  private final Scheduler scheduler;

  @Getter @Setter private volatile FileSystem fileSystem;

  @Getter private final ModuleManager moduleManager;

  @Getter private final ExecutorService executorService;

  @Inject
  public SunshowerKernel(
      ModuleManager moduleManager, Scheduler scheduler, ExecutorService executorService) {
    this.scheduler = scheduler;
    this.moduleManager = moduleManager;
    this.executorService = executorService;
    ((ThreadPoolExecutor) executorService)
        .setRejectedExecutionHandler(
            new RejectedExecutionHandler() {
              @Override
              public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {}
            });
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
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
  public void scheduleTask(ConcurrentProcess process) {
    scheduler.scheduleTask(process);
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  public void start() {
    if (scheduler.isRunning()) {
      throw new IllegalStateException("Can't call start");
    }
    scheduler.start();
    scheduler.registerHandler(ModuleEntryWriteProcessor.getInstance());
    val process = new KernelStartProcess(this);
    scheduler.registerHandler(process);
    scheduler.scheduleTask(process);
  }

  @Override
  public void reload() {
    stop();
    start();
  }

  @Override
  public void stop() {
    if (!scheduler.isRunning()) {
      throw new IllegalStateException("Kernel is not running");
    }
    scheduler.unregisterHandler(ModuleEntryWriteProcessor.getInstance());

    val process = new KernelStopProcess(this);
    scheduler.registerHandler(process);
    scheduler.scheduleTask(process);
    scheduler.awaitShutdown();
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
