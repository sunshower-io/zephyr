package io.sunshower.kernel.launch;

import io.sunshower.kernel.core.SunshowerKernel;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class KernelLauncher {

  public static void main(String[] args) throws IOException {
    new Banner().print(System.out);
    val options = new KernelOptions();
    val cli = new CommandLine(options);
    cli.parseArgs(args);
    options.validate();
    SunshowerKernel.setKernelOptions(options);

    LauncherInjectionModule module = createModule(options);
    val shell = module.shell();
    shell.start();
    System.exit(0);
  }

  static LauncherInjectionModule createModule(KernelOptions options) {
    return DaggerLauncherInjectionModule.builder()
        .launcherInjectionConfiguration(new LauncherInjectionConfiguration(options))
        .build();
  }
}
