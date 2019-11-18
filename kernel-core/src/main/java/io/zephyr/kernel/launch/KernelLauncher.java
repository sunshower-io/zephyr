package io.zephyr.kernel.launch;

import io.zephyr.kernel.core.DaggerSunshowerKernelConfiguration;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.shell.ShellConsole;
import io.zephyr.kernel.shell.ShellExitException;
import io.zephyr.kernel.shell.commands.RestartException;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;

import lombok.val;
import picocli.CommandLine;

@SuppressWarnings({"PMD.UseVarargs", "PMD.ArrayIsStoredDirectly", "PMD.DoNotCallSystemExit"})
public class KernelLauncher implements CommandLine.IExecutionExceptionHandler {

  /** static stuff--figure out clean way to handle this */
  static Kernel kernel;

  static ShellConsole console;
  static KernelLauncher instance;

  final String[] args;

  private final KernelOptions options;

  public static KernelLauncher getInstance() {
    return instance;
  }

  public static Kernel getKernel() {
    if (kernel == null) {
      throw new IllegalStateException("Kernel is null");
    }
    return kernel;
  }

  public static void setKernel(Kernel kernel) {
    KernelLauncher.kernel = kernel;
  }

  public static void setConsole(ShellConsole console) {
    KernelLauncher.console = console;
  }

  public KernelLauncher(String[] args) {
    this.args = args;
    options = new KernelOptions();
  }

  public static ShellConsole getConsole() {
    return console;
  }

  public static LocalizableConsole getConsole(Class<?> type) {
    return new LocalizableConsole(console, type);
  }

  void run() throws IOException {
    if (remote()) {

    } else {
      runLocal();
    }
  }

  private boolean remote() {
    try {
      val registry = LocateRegistry.getRegistry(options.getPort());
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private void runLocal() throws IOException {
    new Banner().print(System.out);
    val cli = new CommandLine(options).setExecutionExceptionHandler(this);
    cli.parseArgs(args);
    options.validate();
    LocateRegistry.createRegistry(options.getPort());
    SunshowerKernel.setKernelOptions(options);

    LauncherInjectionModule module = createModule(options);
    val shell = module.shell();
    module.launcherContext();
    shell.start();
    System.exit(0);
  }

  public static void main(String[] args) throws IOException {
    instance = new KernelLauncher(args);
    instance.run();
  }

  static LauncherInjectionModule createModule(KernelOptions options) {
    return DaggerLauncherInjectionModule.factory().create(options, createKernel(options));
  }

  private static Kernel createKernel(KernelOptions options) {
    return DaggerSunshowerKernelConfiguration.factory()
        .create(options, ClassLoader.getSystemClassLoader())
        .kernel();
  }

  @Override
  public int handleExecutionException(
      Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
    if (ex instanceof ShellExitException) {
      System.out.println("Goodbye!");
      System.exit(0);
    }
    if (ex instanceof RestartException) {
      if (kernel == null) {
        System.out.println("Kernel is not running");
      } else {
        kernel.stop();
        kernel = null;
        main(args);
      }
    }
    return 0;
  }
}
