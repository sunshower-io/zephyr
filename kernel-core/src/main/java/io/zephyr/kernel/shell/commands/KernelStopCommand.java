package io.zephyr.kernel.shell.commands;

import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.shell.Color;
import io.zephyr.kernel.shell.Command;
import picocli.CommandLine;

@SuppressWarnings("PMD.DoNotUseThreads")
@CommandLine.Command(name = "stop")
public class KernelStopCommand extends Command {

  public KernelStopCommand() {
    super(KernelEventTypes.KERNEL_SHUTDOWN_INITIATED, KernelEventTypes.KERNEL_SHUTDOWN_SUCCEEDED);
  }

  @Override
  protected int execute() {
    kernel.stop();
    return 0;
  }

  @Override
  public void onEvent(EventType type, Event<Object> event) {
    if (type == KernelEventTypes.KERNEL_SHUTDOWN_INITIATED) {
      console.writeln("Initiating kernel shutdown", Color.colors(Color.Green));
    }

    if (type == KernelEventTypes.KERNEL_SHUTDOWN_SUCCEEDED) {
      console.writeln("Kernel shutdown succeeded", Color.colors(Color.Green));
    }
  }
}
