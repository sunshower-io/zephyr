package io.zephyr.kernel.modules.shell.console;

public interface CommandContext {

  <T> T getService(Class<T> service);
}
