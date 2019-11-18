package io.zephyr.kernel.launch;

import dagger.Module;
import dagger.Provides;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.shell.*;
import java.io.File;
import java.util.ServiceLoader;
import java.util.logging.*;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.val;

@Module
public class LauncherInjectionConfiguration {

  @Provides
  @Singleton
  public ShellConsole shellConsole(KernelOptions options) {
    return ServiceLoader.load(ShellConsole.class).findFirst().get();
  }

  @Provides
  @Singleton
  public ShellParser shellParser(Kernel kernel, KernelOptions options, CommandRegistry registry) {
    val delegate =
        ServiceLoader.load(ShellParserFactory.class)
            .findFirst()
            .get()
            .create(kernel, options, registry);
    return delegate;
  }

  @Provides
  @Singleton
  public KernelShell shell(ShellParser parser, KernelOptions options, ShellConsole console) {
    return new KernelShell(parser, console, options);
  }

  @Provides
  @Singleton
  public CommandRegistry commandRegistry() {
    return new DefaultCommandRegistry();
  }

  @Provides
  @Singleton
  public LauncherContext context(
      ShellParser parser,
      ShellConsole console,
      CommandRegistry registry,
      KernelOptions options,
      Kernel kernel) {
    configureLogging(options);
    val ctx = new KernelLauncherContext(kernel, console, registry, options);
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
