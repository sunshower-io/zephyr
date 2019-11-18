package io.zephyr.kernel.shell.commands;

import static io.zephyr.kernel.shell.Color.colors;

import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.launch.KernelLauncher;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.shell.Color;
import io.zephyr.kernel.shell.Command;
import picocli.CommandLine;

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
    KernelLauncher.setKernel(kernel);
    return 0;
  }
}
