package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.EventSource;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public interface ModuleTracker extends EventSource, AutoCloseable {

  void close();

  List<Module> getTracked();

  int getTrackedCount();

  void waitUntil(Predicate<? super Collection<Module>> condition);
}
