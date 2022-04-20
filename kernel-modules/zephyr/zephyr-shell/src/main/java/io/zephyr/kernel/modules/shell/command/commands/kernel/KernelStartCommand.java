package io.zephyr.kernel.modules.shell.command.commands.kernel;

import io.sunshower.lang.events.Event;
import io.sunshower.lang.events.EventListener;
import io.sunshower.lang.events.EventType;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.WorkerPool;
import io.zephyr.kernel.core.DaggerSunshowerKernelConfiguration;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.core.KernelLifecycle;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPoint.ContextEntries;
import io.zephyr.kernel.extensions.PrioritizedExtension;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.modules.shell.ShellOptions;
import io.zephyr.kernel.modules.shell.command.DefaultCommand;
import io.zephyr.kernel.modules.shell.command.DefaultCommandContext;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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

  private static List<EntryPoint> resolveEntryPoints() {
    return ServiceLoader.load(EntryPoint.class, ClassLoader.getSystemClassLoader()).stream()
        .map(ServiceLoader.Provider::get)
        .sorted(PrioritizedExtension::compareTo)
        .filter(EntryPoint::requiresKernel)
        .collect(Collectors.toList());
  }

  @Override
  public Result execute(CommandContext context) {

    val options = context.getService(ShellOptions.class);
    val console = context.getService(Console.class);

    if (!(arguments == null || arguments.length == 0)) {
      CommandLine.populateCommand(options, arguments);
      options.validate();
    }

    val kernelOptions = getKernelOptions(options);
    Kernel kernel = context.getService(Kernel.class);

    if (kernel == null) {
      kernel =
          DaggerSunshowerKernelConfiguration.factory()
              .create(
                  kernelOptions,
                  ClassLoader.getSystemClassLoader(),
                  createWorkerPool(kernelOptions, context))
              .kernel();
    }
    val listener = new KernelStartEventHandler(context);
    try {
      kernel.addEventListener(
          listener,
          KernelEventTypes.KERNEL_START_INITIATED,
          KernelEventTypes.KERNEL_START_SUCCEEDED,
          KernelEventTypes.KERNEL_START_FAILED);

      val lifecycle = kernel.getLifecycle();
      if (lifecycle.getState() != KernelLifecycle.State.Stopped) {
        return Result.failure();
      }
      kernel.start();
      ((DefaultCommandContext) context).register(Kernel.class, kernel);
      runEntryPoints(resolveEntryPoints(), (DefaultCommandContext) context, kernel);
      kernel.restoreState().toCompletableFuture().get();
    } catch (Exception ex) {
      if (console != null) {
        console.errorln("Failed to restore kernel state.  Reason: ", ex.getMessage());
      }
    } finally {
      kernel.removeEventListener(listener);
    }
    return Result.success();
  }

  private void runEntryPoints(
      List<EntryPoint> entryPoints, DefaultCommandContext context, Kernel kernel) {

    for (val entryPoint : entryPoints) {
      entryPoint.initialize(context.getLaunchContext());
    }
    val ctx = context.getLaunchContext();
    ctx.put(ContextEntries.KERNEL, kernel);
    val list = new ArrayList<>();
    ctx.put(ContextEntries.RUNNING_ENTRY_POINTS, list);

    for (val entryPoint : entryPoints) {
      list.add(entryPoint);
      kernel.getScheduler().getKernelExecutor().submit(() -> entryPoint.run(ctx));
    }
  }

  private WorkerPool createWorkerPool(KernelOptions kernelOptions, CommandContext context) {
    val kernelService =
        (ExecutorService)
            context.getLaunchContext().get(EntryPoint.ContextEntries.KERNEL_EXECUTOR_SERVICE);

    val gyreService =
        new ThreadPoolExecutor(
            0, kernelOptions.getConcurrency(), 30L, TimeUnit.SECONDS, new LinkedTransferQueue<>());
    return new ExecutorWorkerPool(gyreService, kernelService);
  }

  private KernelOptions getKernelOptions(ShellOptions options) {
    val kernelOptions = new KernelOptions();

    if (arguments != null) {
      val cli = new CommandLine(kernelOptions).setUnmatchedArgumentsAllowed(true);
      cli.parseArgs(arguments);
    }

    if (kernelOptions.getHomeDirectory() == null) {
      kernelOptions.setHomeDirectory(options.getHomeDirectory());
    }
    return kernelOptions;
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
