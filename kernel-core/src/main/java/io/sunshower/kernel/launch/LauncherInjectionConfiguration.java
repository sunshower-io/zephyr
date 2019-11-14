package io.sunshower.kernel.launch;

import dagger.Module;
import dagger.Provides;
import io.sunshower.kernel.shell.*;

import java.io.File;
import java.util.ServiceLoader;
import java.util.logging.*;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.val;

@Module
public class LauncherInjectionConfiguration {

  private final KernelOptions options;

  public LauncherInjectionConfiguration(@NonNull KernelOptions options) {
    this.options = options;
    configureLogging(options);
  }

  @Provides
  @Singleton
  public ShellConsole shellConsole() {
    return ServiceLoader.load(ShellConsole.class).findFirst().get();
  }

  @Provides
  @Singleton
  public ShellParser shellParser() {
    return ServiceLoader.load(ShellParser.class).findFirst().get();
  }

  @Provides
  @Singleton
  public KernelShell shell(ShellParser parser, LauncherContext context, ShellConsole console) {
    return new KernelShell(parser, console, options);
  }

  @Provides
  @Singleton
  public KernelOptions kernelOptions() {
    options.validate();
    return options;
  }

  @Provides
  @Singleton
  public LauncherContext context(ShellParser registry, ShellConsole console) {
    val ctx = new KernelLauncherContext(console, registry, options);
    val loader = ServiceLoader.load(LauncherDecorator.class);
    val iter = loader.iterator();
    while (iter.hasNext()) {
      iter.next().decorate(ctx);
    }
    return ctx;
  }

  private void configureLogging(@NonNull KernelOptions options) {
    try {
      try (val stream = ClassLoader.getSystemResourceAsStream("conf/logging.properties")) {
        LogManager.getLogManager().readConfiguration(stream);
      }
      val logdir = new File(options.getHomeDirectory(), "logs");
      val logFile = new File(logdir, "zephyr.log");
      if (!(logdir.exists() || logdir.mkdirs())) {
        Logger.getGlobal()
            .log(Level.WARNING, "failed to create log directory '%s' ", logdir.getAbsoluteFile());
      }
      for (val handler : Logger.getLogger("").getHandlers()) {
        Logger.getLogger("").removeHandler(handler);
      }

      val handler = new FileHandler(logFile.getAbsolutePath());
      handler.setLevel(Level.INFO);
      handler.setFormatter(new SimpleFormatter());
      Logger.getLogger("").addHandler(handler);

    } catch (Exception ex) {
      Logger.getGlobal().log(Level.WARNING, "failed to configure logging", ex);
    }
  }
}
