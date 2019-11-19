package io.zephyr.kernel.launch.actions;

import io.zephyr.kernel.launch.KernelOptions;

/** simply execute a command on the command line */
public class ExecuteAction implements Runnable {
  final KernelOptions options;

  public ExecuteAction(KernelOptions options) {
    this.options = options;
  }

  @Override
  public void run() {}
}
