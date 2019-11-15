package io.sunshower.kernel.events;

public interface Event<T> {
    T getTarget();
}
