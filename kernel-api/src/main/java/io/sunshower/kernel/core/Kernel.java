package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Scheduler;
import java.util.List;

public interface Kernel {

  ClassLoader getClassLoader();

  ModuleManager getModuleManager();

  <T> List<T> locateServices(Class<T> type);

  void scheduleTask(ConcurrentProcess process);

  Scheduler getScheduler();
}
