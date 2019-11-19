package io.zephyr.kernel.command.kernel;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Parameters;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.AbstractCommand;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import lombok.val;

public class StartKernelCommand extends AbstractCommand implements EventListener<Kernel> {

  public static final String name = "kernel:commands:start";

  public StartKernelCommand() {
    super(name);
  }

  @Override
  public Result invoke(CommandContext context, Parameters parameters) {
    val kernel = context.getKernel();
    try {
      kernel.addEventListener(
          this,
          KernelEventTypes.KERNEL_START_INITIATED,
          KernelEventTypes.KERNEL_START_FAILED,
          KernelEventTypes.KERNEL_START_SUCCEEDED);
      kernel.start();
    } finally {
      kernel.removeEventListener(this);
    }
    return null;
  }

  @Override
  public void onEvent(EventType type, Event<Kernel> event) {
    System.out.println("Sup");
  }

}
