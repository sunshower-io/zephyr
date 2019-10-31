package io.sunshower.kernel.launch;

import lombok.val;
import picocli.CommandLine;

public class KernelLauncher {

  public static void main(String[] args) {
    val options = new KernelOptions();
    val cli = new CommandLine(options);
    cli.parseArgs(args);

    LauncherInjectionModule module = createModule(options);
    val shell = module.shell();
    shell.start();
  }

  static LauncherInjectionModule createModule(KernelOptions options) {
    return DaggerLauncherInjectionModule.builder()
        .launcherInjectionConfiguration(new LauncherInjectionConfiguration(options))
        .build();
  }
}
