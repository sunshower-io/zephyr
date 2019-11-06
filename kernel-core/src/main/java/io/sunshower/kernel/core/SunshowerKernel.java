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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
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

  private final KernelLifecycle lifecycle;

  @Inject
  public SunshowerKernel(
      ModuleManager moduleManager, Scheduler scheduler, ExecutorService executorService) {
    this.scheduler = scheduler;
    this.moduleManager = moduleManager;
    this.executorService = executorService;
    lifecycle = new DefaultLifecycle();
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
  public void scheduleTask(ConcurrentProcess process) {
    scheduler.scheduleTask(process);
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  @SneakyThrows
  public void start() {
    val lcycle = lifecycle.start();
    if (lcycle != null) {
      scheduler.synchronize();
    }
  }

  @Override
  public void reload() {
    stop();
    start();
  }

  @Override
  @SneakyThrows
  public void stop() {
    val lcycle = lifecycle.stop();
    if (lcycle != null) {
      scheduler.synchronize();
    }
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

  class DefaultLifecycle implements KernelLifecycle {

    volatile State state;

    @Override
    public State getState() {
      return state;
    }

    @Override
    public CompletableFuture<Void> stop() {
      if (state == State.Stopped || state == State.Stopping) {
        return null;
      }
      scheduler.unregisterHandler(ModuleEntryWriteProcessor.getInstance());

      val process = new KernelStopProcess(SunshowerKernel.this, this);
      scheduler.registerHandler(process);
      return scheduler.scheduleTask(process);
    }

    @Override
    public CompletableFuture<Void> start() {
      if (state == State.Starting || state == State.Running) {
        return null;
      }
      state = State.Starting;
      scheduler.start();
      scheduler.registerHandler(ModuleEntryWriteProcessor.getInstance());
      val process = new KernelStartProcess(SunshowerKernel.this, this);
      scheduler.registerHandler(process);
      return scheduler.scheduleTask(process);
    }

    @Override
    public CompletableFuture<Void> setState(State state) {
      return null;
    }
  }
}
