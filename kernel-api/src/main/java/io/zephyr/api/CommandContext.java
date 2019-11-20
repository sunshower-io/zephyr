package io.zephyr.api;

public interface CommandContext {

  <T> T getService(Class<T> service);
}
