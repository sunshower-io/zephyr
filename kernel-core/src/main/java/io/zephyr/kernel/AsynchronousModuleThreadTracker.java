package io.zephyr.kernel;

import io.zephyr.api.ModuleTracker;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;

public class AsynchronousModuleThreadTracker implements ModuleTracker {
    @Override
    public <T> void addEventListener(EventListener<T> listener, EventType... types) {

    }

    @Override
    public <T> void removeEventListener(EventListener<T> listener) {

    }

    @Override
    public <T> void dispatchEvent(EventType type, Event<T> event) {

    }
}
