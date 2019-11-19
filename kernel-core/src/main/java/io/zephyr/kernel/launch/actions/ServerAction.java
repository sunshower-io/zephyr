package io.zephyr.kernel.launch.actions;

import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.server.ServerInjectionConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerAction implements Runnable {
  static final Logger log = Logging.get(ServerAction.class);

  final KernelOptions options;

  public ServerAction(KernelOptions options) {
    this.options = options;
  }

  @Override
  public void run() {
    log.log(Level.INFO, "server.action.starting", options.getPort());


  }
}
