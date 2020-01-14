package io.zephyr.kernel.modules.shell;

import io.zephyr.kernel.Options;
import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.modules.shell.console.Console;
import java.util.Map;
import java.util.logging.Logger;

public class ZephyrCliEntryPoint implements EntryPoint {

  static Logger log = Logger.getLogger(ZephyrCliEntryPoint.class.getName());
  //  final ShellOptions options;

  /** private state */
  private Console console;

  public ZephyrCliEntryPoint() {
    //    this.options = args;
  }

  //  @Override
  //  public void run() {
  //    options.validate();
  //    synchronized (ZephyrCliEntryPoint.class) {
  //      if (options.isServer()) {
  //        startServer();
  //      } else if (options.isInteractive()) {
  //        runInteractive();
  //      } else {
  //        runCommand();
  //      }
  //    }
  //  }

  private void runInteractive() {}

  private void runCommand() {}

  private void startServer() {}

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  public void initialize(Map<ContextEntries, Object> context) {
    System.out.println(context);
  }

  @Override
  public void finalize(Map<ContextEntries, Object> context) {}

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public <T> T getService(Class<T> type) {
    return null;
  }

  @Override
  public <T> boolean exports(Class<T> type) {
    return false;
  }

  @Override
  public void run(Map<ContextEntries, Object> ctx) {}

  @Override
  public Options<?> getOptions() {
    return null;
  }

  @Override
  public int getPriority() {
    return 0;
  }

  //  Invoker getInvoker() {
  //    synchronized (ZephyrCliEntryPoint.class) {
  //      val registry = RMI.getRegistry(options);
  //      try {
  //        if (console == null) {
  //          // this is a bit weird--we're instantiating a lot to get the console--nothing else
  //          val shellcfg =
  //              DaggerShellInjectionConfiguration.factory()
  //                  .create(ClassLoader.getSystemClassLoader(), context);
  //          val result = shellcfg.createShell();
  //          console = result.getConsole();
  //        }
  //
  //        return (Invoker) registry.lookup("ZephyrShell");
  //      } catch (Exception e) {
  //        log.log(Level.INFO, "Server isn't running");
  //      }
  //
  //      // context is only to be used by the local shell--remote shell is set in startServer()
  //      val shellcfg =
  //          DaggerShellInjectionConfiguration.factory()
  //              .create(ClassLoader.getSystemClassLoader(), context);
  //
  //      val result = shellcfg.createShell();
  //      context.register(Invoker.class, result);
  //      if (console == null) {
  //        try {
  //          console = result.getConsole();
  //        } catch (Exception ex) {
  //          log.log(Level.WARNING, "Failed to create console", ex.getMessage());
  //        }
  //      }
  //      return result;
  //    }
  //  }

  /** START COPY/PASTE */

  //  static final Logger log = Logger.getLogger(KernelLauncher.class.getName());
  //  private Console console;
  //
  //  final String[] arguments;
  //  final KernelOptions options;
  //  final DefaultCommandContext context;
  //
  //  final Object lock = new Object();
  //
  //  KernelLauncher(final KernelOptions options, final String[] arguments) {
  //    this.options = options;
  //    this.arguments = arguments;
  //    this.context = new DefaultCommandContext();
  //  }
  //
  //  public CommandContext getContext() {
  //    return context;
  //  }
  //
  //  void run() {
  //    options.validate();
  //    synchronized (lock) {
  //      if (options.isServer()) {
  //        startServer();
  //      } else if (options.isInteractive()) {
  //        runInteractive();
  //      } else {
  //        runCommand();
  //      }
  //    }
  //  }
  //
  //  private void runInteractive() {
  //    try {
  //      console = new ColoredConsole();
  //      new Banner(console).print();
  //    } catch (Exception ex) {
  //      log.warning("Can't print banner");
  //    }
  //    context.register(KernelOptions.class, options);
  //    new InteractiveShell(getInvoker(), options, console).start();
  //  }
  //
  //  private void runCommand() {
  //    try {
  //      getInvoker().invoke(Parameters.of(arguments));
  //    } catch (Exception e) {
  //      log.log(Level.WARNING, "Encountered exception while trying to run command",
  // e.getMessage());
  //    }
  //  }
  //
  //  Invoker getInvoker() {
  //    synchronized (lock) {
  //      val registry = RMI.getRegistry(options);
  //      try {
  //        if (console == null) {
  //          // this is a bit weird--we're instantiating a lot to get the console--nothing else
  //          val shellcfg =
  //              DaggerShellInjectionConfiguration.factory()
  //                  .create(ClassLoader.getSystemClassLoader(), context);
  //          val result = shellcfg.createShell();
  //          console = result.getConsole();
  //        }
  //
  //        return (Invoker) registry.lookup("ZephyrShell");
  //      } catch (Exception e) {
  //        log.log(Level.INFO, "Server isn't running");
  //      }
  //
  //      // context is only to be used by the local shell--remote shell is set in startServer()
  //      val shellcfg =
  //          DaggerShellInjectionConfiguration.factory()
  //              .create(ClassLoader.getSystemClassLoader(), context);
  //
  //      val result = shellcfg.createShell();
  //      context.register(Invoker.class, result);
  //      if (console == null) {
  //        try {
  //          console = result.getConsole();
  //        } catch (Exception ex) {
  //          log.log(Level.WARNING, "Failed to create console", ex.getMessage());
  //        }
  //      }
  //      return result;
  //    }
  //  }
  //
  //  @SneakyThrows
  //  @SuppressWarnings("PMD.SystemPrintln")
  //  private void startServer() {
  //    try {
  //      LocateRegistry.createRegistry(options.getPort());
  //    } catch (RemoteException ex) {
  //      System.out.println("Server is already running on port " + options.getPort());
  //    }
  //
  //    val console = new RecordingConsole();
  //    context.register(KernelOptions.class, options);
  //    context.register(Console.class, console);
  //
  //    val invoker =
  //        DaggerShellInjectionConfiguration.factory()
  //            .create(ClassLoader.getSystemClassLoader(), context)
  //            .createShell();
  //    val server =
  //        DaggerServerInjectionConfiguration.factory().build(options, invoker, console).server();
  //    new Banner(invoker.getConsole()).print();
  //    context.register(Server.class, server);
  //    context.register(Invoker.class, invoker);
  //    invoker.setConsole(console);
  //    server.start();
  //  }
  //
  //  static KernelLauncher prepare(String[] args) {
  //    val options = CommandLine.populateSpec(KernelOptions.class, args);
  //    return new KernelLauncher(options, args);
  //  }
  //
  //  public static void main(String[] args) {
  //    prepare(args).run();
  //  }
}
