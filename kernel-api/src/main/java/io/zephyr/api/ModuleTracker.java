package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.EventSource;
import java.io.Closeable;
import java.util.List;
import java.util.function.Predicate;

public interface ModuleTracker extends EventSource, Closeable {

  List<Module> getTracked();

  int getTrackedCount();

  void waitUntil(Predicate<List<Module>> condition);
}
