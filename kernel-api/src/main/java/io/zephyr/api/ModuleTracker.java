package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.EventSource;
import java.io.Closeable;
import java.util.List;

public interface ModuleTracker extends EventSource, Closeable {

  List<Module> getTracked();

  int getTrackedCount();
}
