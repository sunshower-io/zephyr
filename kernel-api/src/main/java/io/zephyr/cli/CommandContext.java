package io.zephyr.cli;

public interface CommandContext {

  <T> T getService(Class<T> service);
}
