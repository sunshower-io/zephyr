package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.KernelEventTypes;
import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventType;
import io.sunshower.kernel.shell.Color;
import io.sunshower.kernel.shell.Command;
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
