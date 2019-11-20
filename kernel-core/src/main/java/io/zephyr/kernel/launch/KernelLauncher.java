package io.zephyr.kernel.launch;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Invoker;
import io.zephyr.api.Parameters;
import io.zephyr.kernel.command.DaggerShellInjectionConfiguration;
import io.zephyr.kernel.command.DefaultCommandContext;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.server.DaggerServerInjectionConfiguration;
import io.zephyr.kernel.server.Server;
import lombok.val;
import picocli.CommandLine;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

@SuppressFBWarnings
@SuppressWarnings({"PMD.UseVarargs", "PMD.ArrayIsStoredDirectly", "PMD.DoNotCallSystemExit"})
public class KernelLauncher {

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
    } else {
      runCommand();
    }
  }

  private void runCommand() {
    val registry = RMI.getRegistry(options);
    try {
      val shell = (Invoker) registry.lookup("ZephyrShell");
      shell.invoke(Parameters.of(arguments));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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
