package io.sunshower.kernel.core;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.launch.KernelOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class SunshowerKernel implements Kernel {

  /** class fields */
  @Setter private static KernelOptions kernelOptions;

  /** Instance fields */
  @Getter private final ModuleManager moduleManager;

  @Inject
  public SunshowerKernel(ModuleManager moduleManager) {
    this.moduleManager = moduleManager;
  }

  public static KernelOptions getKernelOptions() {
    if (kernelOptions == null) {
      throw new IllegalStateException("Error: KernelOptions are null--this is definitely a bug");
    }
    return kernelOptions;
  }

  @Override
  public ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  @Override
  public <T> List<T> locateServices(Class<T> type) {
    val result = new ArrayList<T>();
    load(result, type, getClassLoader());

    val modules = moduleManager.getModules(Module.Type.KernelModule);
    for (val module : modules) {
      val loader = module.resolveServiceLoader(type);
      for (val service : loader) {
        result.add(service);
      }
    }
    return result;
  }

  @Override
  public void scheduleTask(ConcurrentProcess process) {}

  @Override
  public Scheduler getScheduler() {
    return null;
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private <T> void load(List<T> result, Class<T> type, ClassLoader classLoader) {
    val loader = ServiceLoader.load(type, classLoader);
    for (val service : loader) {
      result.add(service);
    }
  }
}
