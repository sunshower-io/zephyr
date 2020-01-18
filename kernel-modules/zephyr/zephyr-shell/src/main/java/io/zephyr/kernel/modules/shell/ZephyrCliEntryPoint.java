package io.zephyr.kernel.modules.shell;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.modules.shell.command.ColoredConsole;
import io.zephyr.kernel.modules.shell.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.modules.shell.command.DefaultCommandContext;
import io.zephyr.kernel.modules.shell.console.Console;
import io.zephyr.kernel.modules.shell.console.Invoker;
import io.zephyr.kernel.modules.shell.console.Parameters;
import io.zephyr.kernel.modules.shell.server.DaggerServerInjectionConfiguration;
import io.zephyr.kernel.modules.shell.server.Server;
import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.SneakyThrows;
import lombok.val;

public class ZephyrCliEntryPoint implements EntryPoint {

  static Logger log = Logger.getLogger(ZephyrCliEntryPoint.class.getName());

  /** private state */
  private Console console;

  private String[] arguments;
  private ShellOptions options;

  private DefaultCommandContext context;

  public ZephyrCliEntryPoint() {}

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    options = io.zephyr.common.Options.create(ShellOptions::new, context);
    arguments = (String[]) context.get(ContextEntries.ARGS);
    synchronized (this) {
      this.context = new DefaultCommandContext(context);
      notifyAll();
    }
  }

  @Override
  public void finalize(Map<ContextEntries, Object> context) {}

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public <T> T getService(Class<T> type) {
    doWait();
    return context.getService(type);
  }

  @Override
  public <T> boolean exports(Class<T> type) {
    doWait();
    return context.getService(type) != null;
  }

  @Override
  public void run(Map<ContextEntries, Object> ctx) {
    options.validate();
    if (options.isServer()) {
      startServer();
    } else if (options.isInteractive()) {
      runInteractive();
    } else {
      runCommand();
    }
  }

  @Override
  public ShellOptions getOptions() {
    return options;
  }

  @Override
  public int getPriority() {
    return 0;
  }

  private void runInteractive() {
    try {
      console = new ColoredConsole();
    } catch (Exception ex) {
      log.warning("Can't print banner");
    }
    context.register(ShellOptions.class, options);
    new InteractiveShell(getInvoker(), options, console).start();
  }

  Invoker getInvoker() {
    synchronized (ZephyrCliEntryPoint.class) {
      val registry = RMI.getRegistry(options);
      try {
        if (console == null) {
          // this is a bit weird--we're instantiating a lot to get the console--nothing else
          val shellcfg =
              DaggerShellInjectionConfiguration.factory()
                  .create(ClassLoader.getSystemClassLoader(), context);
          val result = shellcfg.createShell();
          console = result.getConsole();
        }

        return (Invoker) registry.lookup("ZephyrShell");
      } catch (Exception e) {
        log.log(Level.INFO, "Server isn't running");
      }

      // context is only to be used by the local shell--remote shell is set in startServer()
      val shellcfg =
          DaggerShellInjectionConfiguration.factory()
              .create(ClassLoader.getSystemClassLoader(), context);

      val result = shellcfg.createShell();
      context.register(Invoker.class, result);
      if (console == null) {
        try {
          console = result.getConsole();
        } catch (Exception ex) {
          log.log(Level.WARNING, "Failed to create console", ex.getMessage());
        }
      }
      return result;
    }
  }

  static boolean created = false;

  @SneakyThrows
  @SuppressWarnings("PMD.SystemPrintln")
  private void startServer() {
    try {
      LocateRegistry.createRegistry(options.getPort());
    } catch (Exception ex) {

    }

    val console = new RecordingConsole();
    context.register(ShellOptions.class, options);
    context.register(Console.class, console);

    val invoker =
        DaggerShellInjectionConfiguration.factory()
            .create(ClassLoader.getSystemClassLoader(), context)
            .createShell();
    val server =
        DaggerServerInjectionConfiguration.factory().build(options, invoker, console).server();
    context.register(Server.class, server);
    context.register(Invoker.class, invoker);
    invoker.setConsole(console);
    server.start();
  }

  private void runCommand() {
    try {
      getInvoker().invoke(Parameters.of(arguments));
    } catch (Exception e) {
      log.log(Level.WARNING, "Encountered exception while trying to run command", e.getMessage());
    }
  }

  /** wait for kernellauncher to start */
  private void doWait() {
    while (context == null) {
      synchronized (this) {
        try {
          wait(200);
        } catch (InterruptedException ex) {
        }
      }
    }
  }
}
