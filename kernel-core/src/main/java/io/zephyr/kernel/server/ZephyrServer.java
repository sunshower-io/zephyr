package io.zephyr.kernel.server;

import io.zephyr.api.Invoker;
import io.zephyr.kernel.launch.KernelOptions;

public class ZephyrServer implements Server {

  private volatile boolean running;

  //  private final Invoker invoker;
  //  private final KernelOptions options;
  //
  //  public ZephyrServer(Invoker invoker, KernelOptions options) {
  //    this.invoker = invoker;
  //    this.options = options;
  //  }
  //
  @Override
  public void start() {}

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public void stop() {}
}
