package io.zephyr.logging;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.launch.KernelLauncher;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class LogEntryPoint implements EntryPoint {

  @Override
  public Logger getLogger() {
    return Logger.getAnonymousLogger();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize(Map<ContextEntries, Object> context) {
    val launcher = getKernelLauncher(context);
    val options = launcher.getOptions();

    if (options.getHomeDirectory() != null) {

      val logdir = new File(options.getHomeDirectory().getAbsoluteFile(), "logs");
      if (!logdir.exists()) {
        if (!logdir.mkdirs()) {
          getLogger().log(Level.SEVERE, "Could not make log directory " + logdir.getAbsolutePath());
          return;
        }
      }
      val logpattern = new File(logdir, "zephyr.log");

      Logger root = getRootAndClearHandlers();
      try {
        val handler = new FileHandler(logpattern.getAbsolutePath());
        handler.setFormatter(new LogFormatter());
        root.addHandler(handler);
      } catch (IOException ex) {
        getLogger().log(Level.SEVERE, "Failed to configure logging.  Reason: {0}", ex.getMessage());
        getLogger().log(Level.SEVERE, "Full cause: ", ex);
      }
    }
  }

  Logger getRootAndClearHandlers() {
    val logger = getRootLogger();

    val handlers = logger.getHandlers();
    for (val handler : handlers) {
      logger.removeHandler(handler);
    }
    return logger;
  }

  Logger getRootLogger() {
    Logger logger;
    for (logger = getLogger(); logger.getParent() != null; logger = logger.getParent()) {}
    return logger;
  }

  @Override
  public Options<?> getOptions() {
    return Options.EMPTY;
  }

  @Override
  public int getPriority() {
    return EntryPoint.HIGHEST_PRIORITY + 11;
  }

  private KernelLauncher getKernelLauncher(Map<ContextEntries, Object> context) {
    val entryPoints = (List<EntryPoint>) context.get(ContextEntries.ENTRY_POINTS);
    for (val entryPoint : entryPoints) {
      if (KernelLauncher.class.isAssignableFrom(entryPoint.getClass())) {
        return (KernelLauncher) entryPoint;
      }
    }
    throw new NoSuchElementException("Can't configure logging--no launcher");
  }
}
