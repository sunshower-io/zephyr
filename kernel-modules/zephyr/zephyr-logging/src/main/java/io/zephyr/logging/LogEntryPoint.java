package io.zephyr.logging;


import io.zephyr.common.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.zephyr.kernel.log.Logging;
import lombok.val;

public class LogEntryPoint implements EntryPoint {

  static final Logger log = Logging.get(LogEntryPoint.class);

  private LogOptions options;

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void initialize(Map<ContextEntries, Object> context) {
    options = Options.create(LogOptions::new, context);

    if (options.getHomeDirectory() != null) {

      val logdir = new File(options.getHomeDirectory().getAbsoluteFile(), "logs");
      if (!logdir.exists()) {
        if (!logdir.mkdirs()) {
          log.log(Level.SEVERE, "log.entrypoint.mkdir.failed", logdir.getAbsolutePath());
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
        log.log(Level.SEVERE, "log.entrypoint.configuration.failed", ex.getMessage());
        log.log(Level.SEVERE, "log.entrypoint.configuration.failed.ex", ex);
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
  public LogOptions getOptions() {
    return options;
  }

  @Override
  public int getPriority() {
    return EntryPoint.HIGHEST_PRIORITY + 4;
  }
}
