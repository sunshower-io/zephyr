package io.zephyr.kernel.modules.shell;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.modules.shell.console.Console;

public class ZephyrCliEntryPoint implements EntryPoint {

  final ShellOptions options;

  /** private state */
  private Console console;

  public ZephyrCliEntryPoint(ShellOptions args) {
    this.options = args;
  }

  @Override
  public void run() {
    options.validate();
    synchronized (ZephyrCliEntryPoint.class) {
      if (options.isServer()) {
        startServer();
      } else if (options.isInteractive()) {
        runInteractive();
      } else {
        runCommand();
      }
    }
  }

  private void runInteractive() {}

  private void runCommand() {}

  private void startServer() {}

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
}
