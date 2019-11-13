package io.sunshower.kernel.launch;

import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.shell.ShellConsole;
import io.sunshower.kernel.shell.ShellExitException;
import io.sunshower.kernel.shell.commands.RestartException;
import lombok.val;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    instance = this;
    options = new KernelOptions();
  }

  public static ShellConsole getConsole() {
    return console;
  }

  public static LocalizableConsole getConsole(Class<?> type) {
    return new LocalizableConsole(console, type);
  }

  void run() throws IOException {
    new Banner().print(System.out);
    val cli = new CommandLine(options).setExecutionExceptionHandler(this);
    cli.parseArgs(args);
    options.validate();
    SunshowerKernel.setKernelOptions(options);

    LauncherInjectionModule module = createModule(options);
    val shell = module.shell();
    shell.start();
    System.exit(0);
  }

  public static void main(String[] args) throws IOException {
    new KernelLauncher(args).run();
  }

  static LauncherInjectionModule createModule(KernelOptions options) {
    return DaggerLauncherInjectionModule.builder()
        .launcherInjectionConfiguration(new LauncherInjectionConfiguration(options))
        .build();
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
        instance = null;
        List<String> args = new ArrayList<>();
        if (options.getHomeDirectory() != null) {
          args.add("-h ");
          args.add(options.getHomeDirectory().getAbsolutePath());
        }
        if (options.getConcurrency() != null) {
          args.add("-c ");
          args.add(String.valueOf(options.getConcurrency()));
        }
        if (options.isInteractive()) {
          args.add("-i ");
        }
        System.gc();
        new KernelLauncher(args.toArray(new String[0])).run();
      }
    }

    return 0;
  }
}
