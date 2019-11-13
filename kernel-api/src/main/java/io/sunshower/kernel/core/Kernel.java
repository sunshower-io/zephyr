package io.sunshower.kernel.core;

import io.sunshower.PluginContext;
import io.sunshower.kernel.concurrency.Scheduler;
import java.nio.file.FileSystem;
import java.util.List;

public interface Kernel extends PluginContext {

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
}
