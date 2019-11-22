package io.zephyr.kernel.core;

import io.zephyr.PluginContext;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.events.EventSource;
import io.zephyr.kernel.memento.Originator;

import java.nio.file.FileSystem;
import java.util.List;

public interface Kernel extends PluginContext, EventSource, Originator<Kernel> {

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
