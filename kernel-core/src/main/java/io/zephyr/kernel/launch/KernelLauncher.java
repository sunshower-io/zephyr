package io.zephyr.kernel.launch;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import io.zephyr.kernel.extensions.PrioritizedExtension;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.val;
import picocli.CommandLine;

@SuppressFBWarnings
@SuppressWarnings({
  "PMD.UseVarargs",
  "PMD.ArrayIsStoredDirectly",
  "PMD.DoNotCallSystemExit",
  "PMD.FinalizeOverloaded",
  "PMD.AvoidDuplicateLiterals"
})
public class KernelLauncher implements EntryPoint, EntryPointRegistry {

  static final Logger log = Logging.get(KernelLauncher.class);

  private KernelOptions options;
  private ExecutorService executorService;
  private Map<ContextEntries, Object> context;

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public void stop() {
    executorService.shutdown();
    try {
      executorService.awaitTermination(1000, TimeUnit.MICROSECONDS);
    } catch (InterruptedException ex) {
      log.log(Level.INFO, "kernel.launcher.kernel.executor.interrupted");
    }
  }

  @Override
  public void start() {
    int concurrency = getOptions().getKernelConcurrency();
    log.log(Level.INFO, "kernel.launcher.kernel.concurrency", concurrency);
    executorService =
        new ThreadPoolExecutor(0, concurrency, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    this.context = context;
    context.put(ContextEntries.ENTRY_POINT_REGISTRY, this);
    val args = (String[]) context.get(ContextEntries.ARGS);
    options = new KernelOptions();
    val commandLine = new CommandLine(options).setUnmatchedArgumentsAllowed(true);
    final CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
    context.put(ContextEntries.ARGS, parseResult.unmatched().toArray(new String[0]));
  }

  @Override
  public KernelOptions getOptions() {
    return options;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> type) {
    if (ExecutorService.class.isAssignableFrom(type)) {
      return (T) executorService;
    }

    if (EntryPointRegistry.class.isAssignableFrom(type)) {
      return (T) this;
    }
    throw new NoSuchElementException(
        "This kernel module does not export a service of type '" + type + "'");
  }

  @Override
  public <T> boolean exports(Class<T> type) {
    if (ExecutorService.class.isAssignableFrom(type)) {
      return true;
    }

    if (EntryPointRegistry.class.isAssignableFrom(type)) {
      return true;
    }

    return false;
  }

  public static void main(String[] args) {
    log.log(Level.INFO, "kernel.launcher.starting");
    Map<ContextEntries, Object> context = launch(args);
    log.log(Level.INFO, "kernel.launcher.stopping");
    stop(context);
    doFinalize(context);
    log.log(Level.INFO, "kernel.launcher.stopped");
  }

  @SuppressWarnings({"unchecked", "PMD.AvoidCallingFinalize"})
  private static void doFinalize(Map<ContextEntries, Object> context) {
    val entryPoints = new ArrayList<>((List<EntryPoint>) context.get(ContextEntries.ENTRY_POINTS));
    Collections.reverse(entryPoints);
    for (val entryPoint : entryPoints) {
      log.log(Level.INFO, "kernel.entrypoint.finalizing", entryPoint);
      entryPoint.finalize(context);
      log.log(Level.INFO, "kernel.entrypoint.finalized", entryPoint);
    }
  }

  @SuppressWarnings("unchecked")
  private static void stop(Map<ContextEntries, Object> context) {
    val entryPoints = new ArrayList<>((List<EntryPoint>) context.get(ContextEntries.ENTRY_POINTS));
    Collections.reverse(entryPoints);
    for (val entryPoint : entryPoints) {
      log.log(Level.INFO, "kernel.entrypoint.stopping", entryPoint);
      entryPoint.stop();
      log.log(Level.INFO, "kernel.entrypoint.stopped", entryPoint);
    }
  }

  static Map<ContextEntries, Object> launch(String[] args) {
    val loaders =
        ServiceLoader.load(EntryPoint.class, ClassLoader.getSystemClassLoader())
            .stream()
            .map(ServiceLoader.Provider::get)
            .sorted(PrioritizedExtension::compareTo)
            .collect(Collectors.toList());

    val context = new EnumMap<>(EntryPoint.ContextEntries.class);
    context.put(ContextEntries.ARGS, args);
    context.put(ContextEntries.ENTRY_POINTS, loaders);
    initializeAll(loaders.iterator(), context);
    startAll(loaders.iterator());
    ExecutorService kernelExecutor = locateExecutor(loaders.iterator());
    context.put(ContextEntries.KERNEL_EXECUTOR_SERVICE, kernelExecutor);
    runAll(kernelExecutor, loaders, context);
    return context;
  }

  @SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.UnusedPrivateMethod"})
  private static void runAll(
      ExecutorService kernelExecutor, List<EntryPoint> tasks, Map<ContextEntries, Object> context) {
    val completionQueue = new ArrayBlockingQueue<Future<EntryPoint>>(tasks.size());
    val completionService =
        new ExecutorCompletionService<EntryPoint>(kernelExecutor, completionQueue);
    for (val entryPoint : tasks) {
      log.log(Level.INFO, "kernel.entrypoint.scheduling", entryPoint);
      completionService.submit(EntryPoint.wrap(entryPoint, context));
      log.log(Level.INFO, "kernel.entrypoint.scheduled", entryPoint);
    }
    while (!completionQueue.isEmpty()) {
      EntryPoint entryPoint = null;
      try {
        entryPoint = completionQueue.take().get();
        log.log(Level.INFO, "kernel.entrypoint.running.complete", entryPoint);
      } catch (InterruptedException ex) {
        log.log(Level.WARNING, "kernel.entrypoint.running.interrupted");
      } catch (ExecutionException ex) {
        log.log(Level.WARNING, "kernel.entrypoint.running.failed", ex.getMessage());
        log.log(Level.INFO, "kernel.entrypoint.running.failed.ex", ex);
      }
    }
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private static ExecutorService locateExecutor(Iterator<EntryPoint> loaders) {
    log.log(Level.INFO, "kernel.entrypoint.locating.executorservice");
    ExecutorService executorService = null;
    while (loaders.hasNext()) {
      val next = loaders.next();
      if (next.exports(ExecutorService.class)) {
        executorService = next.getService(ExecutorService.class);
        break;
      }
    }

    if (executorService == null) {
      log.log(Level.SEVERE, "kernel.entrypoint.locating.executorservice.failed");
      System.exit(1);
    }
    log.log(Level.INFO, "kernel.entrypoint.locating.executorservice.succeeded", executorService);
    return executorService;
  }

  private static void startAll(Iterator<EntryPoint> iterator) {
    while (iterator.hasNext()) {
      val entrypoint = iterator.next();
      log.log(Level.INFO, "kernel.entrypoint.starting", entrypoint);
      entrypoint.start();
      log.log(Level.INFO, "kernel.entrypoint.started", entrypoint);
    }
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private static void initializeAll(
      Iterator<EntryPoint> iterator, Map<ContextEntries, Object> context) {
    while (iterator.hasNext()) {
      val loader = iterator.next();
      initialize(loader, context);
    }
  }

  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private static void initialize(EntryPoint loader, Map<ContextEntries, Object> context) {
    log.log(Level.INFO, "kernel.entrypoint.initializing", loader);
    loader.initialize(context);
    log.log(Level.INFO, "kernel.entrypoint.initialized", loader);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<EntryPoint> getEntryPoints() {
    return (List<EntryPoint>) context.get(ContextEntries.ENTRY_POINTS);
  }

  @Override
  public List<EntryPoint> getEntryPoints(Predicate<EntryPoint> filter) {
    return getEntryPoints().stream().filter(filter).collect(Collectors.toList());
  }

  @Override
  public int getPriority() {
    return PrioritizedExtension.HIGHEST_PRIORITY;
  }
}
