package io.zephyr.kernel.modules.shell.server;

public interface Server {

  void start();

  boolean isRunning();

  void stop();
}
