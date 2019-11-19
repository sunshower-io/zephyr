package io.zephyr.kernel.server;

public interface Server {

  void start();

  boolean isRunning();

  void stop();
}
