package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.EventSource;

public interface ModuleTracker extends Tracker<Module>, EventSource, AutoCloseable {}
