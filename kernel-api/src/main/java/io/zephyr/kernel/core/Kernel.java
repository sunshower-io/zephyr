package io.zephyr.kernel.core;

import io.zephyr.api.ModuleContext;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.events.EventSource;
import io.zephyr.kernel.memento.Caretaker;
import io.zephyr.kernel.memento.Originator;
import java.nio.file.FileSystem;
import java.util.List;

public interface Kernel extends EventSource, Originator, Caretaker {

  ServiceRegistry getServiceRegistry();

  List<KernelModuleEntry> getKernelModules();

  KernelLifecycle getLifecycle();

  ClassLoader getClassLoader();

  ModuleManager getModuleManager();

  <T> List<T> locateServices(Class<T> type);

  FileSystem getFileSystem();

  void start();

  void reload();

  void stop();

  ModuleClasspathManager getModuleClasspathManager();

  Scheduler<String> getScheduler();

  ModuleContext createContext(Module module);
}
