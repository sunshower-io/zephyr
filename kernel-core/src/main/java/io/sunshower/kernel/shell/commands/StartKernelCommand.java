package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.KernelEventTypes;
import io.sunshower.kernel.events.Event;
import io.sunshower.kernel.events.EventType;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.shell.Color;
import io.sunshower.kernel.shell.Command;
import picocli.CommandLine;

import static io.sunshower.kernel.shell.Color.colors;

@SuppressWarnings("PMD.DoNotUseThreads")
@SuppressFBWarnings
@CommandLine.Command(name = "start")
public final class StartKernelCommand extends Command {
  public StartKernelCommand() {
    super(KernelEventTypes.KERNEL_START_INITIATED, KernelEventTypes.KERNEL_START_SUCCEEDED);
  }

  @Override
  public void onEvent(EventType type, Event event) {
    if (type == KernelEventTypes.KERNEL_START_INITIATED) {
      console.writeln("Kernel Starting...", colors(Color.Green));
    }
    if (type == KernelEventTypes.KERNEL_START_SUCCEEDED) {
      console.writeln("Kernel Started Successfully", colors(Color.Green));
    }
  }

  @Override
  protected int execute() {
    kernel.start();
    return 0;
  }
}
