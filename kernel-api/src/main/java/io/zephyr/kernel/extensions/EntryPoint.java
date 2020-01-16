package io.zephyr.kernel.extensions;

import io.zephyr.api.Startable;
import io.zephyr.api.Stoppable;
import io.zephyr.kernel.Options;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lifecycle of an entrypoint
 *
 * <p>1. initialize with arguments, removing processed arguments to prevent subsequent entrypoints
 * from consuming them 2. start() 3. run() 4. stop() 5. finalize()
 */
public interface EntryPoint extends PrioritizedExtension, Startable, Stoppable {
  enum ContextEntries {
    ARGS,
    ENTRY_POINTS,
    ENTRY_POINTS_TEMP,
    KERNEL_EXECUTOR_SERVICE,
    ENTRY_POINT_REGISTRY;
  }

  Logger getLogger();

  default void initialize(Map<ContextEntries, Object> context) {}

  default void finalize(Map<ContextEntries, Object> context) {}

  default void start() {
    getLogger().log(Level.INFO, "entry point starting...");
  }

  default void stop() {
    getLogger().log(Level.INFO, "entry point stopping...");
  }

  default <T> T getService(Class<T> type) {
    throw new UnsupportedOperationException("Empty Entry Point contains no services");
  }

  default <T> boolean exports(Class<T> type) {
    return false;
  }

  static Callable<EntryPoint> wrap(EntryPoint entryPoint, Map<ContextEntries, Object> context) {
    return new WrappedEntryPointCallable(entryPoint, context);
  }

  default void run(Map<ContextEntries, Object> ctx) {}

  Options<?> getOptions();
}

final class WrappedEntryPointCallable implements Callable<EntryPoint> {
  final EntryPoint entryPoint;
  final Map<EntryPoint.ContextEntries, Object> context;

  WrappedEntryPointCallable(
      final EntryPoint entryPoint, final Map<EntryPoint.ContextEntries, Object> ctx) {
    this.context = ctx;
    this.entryPoint = entryPoint;
  }

  @Override
  public EntryPoint call() throws Exception {
    entryPoint.run(context);
    return entryPoint;
  }
}
