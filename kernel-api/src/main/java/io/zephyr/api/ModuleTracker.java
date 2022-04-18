package io.zephyr.api;

import io.sunshower.lang.events.EventSource;
import io.zephyr.kernel.Module;

public interface ModuleTracker extends Tracker<Module>, EventSource, AutoCloseable {}
