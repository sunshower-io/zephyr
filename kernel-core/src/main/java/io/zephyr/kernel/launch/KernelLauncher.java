package io.zephyr.kernel.launch;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Console;
import io.zephyr.api.Invoker;
import io.zephyr.api.Parameters;
import io.zephyr.kernel.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.command.DefaultCommandContext;
import io.zephyr.kernel.command.ShellInjectionConfiguration;
import io.zephyr.kernel.command.ShellModule;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.server.DaggerServerInjectionConfiguration;
import io.zephyr.kernel.server.Server;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;
import picocli.CommandLine;

@SuppressFBWarnings
@SuppressWarnings({"PMD.UseVarargs", "PMD.ArrayIsStoredDirectly", "PMD.DoNotCallSystemExit"})
public class KernelLauncher {

  static final Logger log = Logger.getLogger(KernelLauncher.class.getName());
  private Console console;

  final String[] arguments;
  final KernelOptions options;
  final DefaultCommandContext context;

  KernelLauncher(final KernelOptions options, final String[] arguments) {
    this.options = options;
    this.arguments = arguments;
    this.context = new DefaultCommandContext();
  }

  public CommandContext getContext() {
    return context;
  }

  void run() {
    if (options.isServer()) {
      startServer();
    } else if (options.isInteractive()) {
      runInteractive();
    } else {
      runCommand();
    }
  }

  private void runInteractive() {
    context.register(KernelOptions.class, options);
    new InteractiveShell(getInvoker(), options, console).start();
  }

  private void runCommand() {
    try {
      getInvoker().invoke(Parameters.of(arguments));
    } catch (Exception e) {
      log.log(Level.WARNING, "Encountered exception while trying to run command", e.getMessage());
    }
  }

  Invoker getInvoker() {
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

  @SuppressWarnings("PMD.SystemPrintln")
  private void startServer() {
    try {
      LocateRegistry.createRegistry(options.getPort());
    } catch (RemoteException ex) {
      System.out.println("Server is already running on port " + options.getPort());
    }

    context.register(KernelOptions.class, options);

    val invoker =
        DaggerShellInjectionConfiguration.factory()
            .create(ClassLoader.getSystemClassLoader(), context)
            .createShell();
    val server = DaggerServerInjectionConfiguration.factory().build(options, invoker).server();
    context.register(Server.class, server);
    context.register(Invoker.class, invoker);
    server.start();
  }

  static KernelLauncher prepare(String[] args) {
    val options = CommandLine.populateSpec(KernelOptions.class, args);
    return new KernelLauncher(options, args);
  }

  public static void main(String[] args) {
    prepare(args).run();
  }
}
