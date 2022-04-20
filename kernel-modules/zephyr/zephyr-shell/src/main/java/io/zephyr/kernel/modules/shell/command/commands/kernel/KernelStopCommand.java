package io.zephyr.kernel.modules.shell.command.commands.kernel;

import io.sunshower.lang.events.Event;
import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventType;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPoint.ContextEntries;
import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class KernelStopCommand extends DefaultCommand {

  private static final long serialVersionUID = -6060440377107158929L;

  public KernelStopCommand() {
    super("stop");
  }

  @Override
  public Result execute(CommandContext context) {

    Kernel kernel = context.getService(Kernel.class);
    Console console = context.getService(Console.class);

    if (kernel == null) {
      console.errorln("Kernel is not running");
      return Result.failure();
    }
    val listener = new KernelStopEventHandler(context);
    try {
      kernel.addEventListener(
          listener,
          KernelEventTypes.KERNEL_SHUTDOWN_INITIATED,
          KernelEventTypes.KERNEL_SHUTDOWN_SUCCEEDED);

      val lifecycle = kernel.getLifecycle();
      if (lifecycle.getState() != KernelLifecycle.State.Running) {
        return Result.failure();
      }
      console.successln("Attempting to save kernel state...");
      kernel.persistState().toCompletableFuture().get();
      console.successln("Successfully wrote kernel state");
      stopRunningEntryPoints(kernel, context);
      kernel.stop();
    } catch (Exception e) {
      console.errorln("Failed to save state: ", e.getMessage());
    } finally {
      kernel.removeEventListener(listener);
    }
    return Result.success();
  }

  @SuppressWarnings("unchecked")
  private void stopRunningEntryPoints(Kernel kernel, CommandContext context) {
    val running =
        (List<EntryPoint>) context.getLaunchContext().get(ContextEntries.RUNNING_ENTRY_POINTS);
    if (running == null || running.isEmpty()) {
      return;
    }

    for (val entryPoint : running) {
      entryPoint.finalize(context.getLaunchContext());
    }
    for (val entryPoint : running) {
      entryPoint.stop();
    }
  }

  @AllArgsConstructor
  private static final class KernelStopEventHandler implements EventListener<Kernel> {

    final CommandContext context;

    @Override
    public void onEvent(EventType type, Event<Kernel> event) {
      val console = context.getService(Console.class);
      val etype = (KernelEventTypes) type;
      switch (etype) {
        case KERNEL_SHUTDOWN_INITIATED:
          console.errorln("Shutting down kernel");
          break;
        case KERNEL_SHUTDOWN_SUCCEEDED:
          console.successln("Successfully shut down Zephyr kernel");
          break;
        default:
          return;
      }
    }
  }
}
