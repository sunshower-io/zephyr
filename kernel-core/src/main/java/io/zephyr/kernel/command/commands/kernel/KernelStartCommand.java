package io.zephyr.kernel.command.commands.kernel;

import static io.zephyr.kernel.core.KernelEventTypes.*;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Result;
import io.zephyr.kernel.command.DefaultCommand;
import io.zephyr.kernel.command.DefaultCommandContext;
import io.zephyr.kernel.core.DaggerSunshowerKernelConfiguration;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.events.EventListener;
import io.zephyr.kernel.events.EventType;
import io.zephyr.kernel.launch.KernelOptions;
import lombok.AllArgsConstructor;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "start")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class KernelStartCommand extends DefaultCommand {

  private static final long serialVersionUID = -8306551865402490711L;
  @CommandLine.Unmatched private String[] arguments;

  public KernelStartCommand() {
    super("start");
  }

  @Override
  public Result execute(CommandContext context) {

    val options = context.getService(KernelOptions.class);

    if (!(arguments == null || arguments.length == 0)) {
      CommandLine.populateCommand(options, arguments);
      options.validate();
    }

    Kernel kernel = context.getService(Kernel.class);

    if (kernel == null) {
      kernel =
          DaggerSunshowerKernelConfiguration.factory()
              .create(options, ClassLoader.getSystemClassLoader())
              .kernel();
    }
    val listener = new KernelStartEventHandler(context);
    try {
      kernel.addEventListener(
          listener, KERNEL_START_INITIATED, KERNEL_START_SUCCEEDED, KERNEL_START_FAILED);

      val lifecycle = kernel.getLifecycle();
      if (lifecycle.getState() != KernelLifecycle.State.Stopped) {
        return Result.failure();
      }
      kernel.start();
      ((DefaultCommandContext) context).register(Kernel.class, kernel);
    } finally {
      kernel.removeEventListener(listener);
    }
    return Result.success();
  }

  @AllArgsConstructor
  private static final class KernelStartEventHandler implements EventListener<Kernel> {
    final CommandContext context;

    @Override
    public void onEvent(EventType type, Event<Kernel> event) {
      val console = context.getService(Console.class);
      val etype = (KernelEventTypes) type;
      switch (etype) {
        case KERNEL_START_FAILED:
          console.errorln("Starting kernel failed");
          break;
        case KERNEL_START_INITIATED:
          console.successln("Starting Zephyr Kernel");
          break;
        case KERNEL_START_SUCCEEDED:
          console.successln("Successfully started Zephyr Kernel");
          break;
        default:
          return;
      }
    }
  }
}
