package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.DaggerSunshowerKernelConfiguration;
import io.sunshower.kernel.core.SunshowerKernelInjectionModule;
import io.sunshower.kernel.launch.KernelLauncher;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import java.util.concurrent.Callable;

import io.sunshower.kernel.shell.KernelLauncherContext;
import lombok.Setter;
import lombok.val;
import picocli.CommandLine;

@SuppressWarnings("PMD.DoNotUseThreads")
@SuppressFBWarnings
@CommandLine.Command(name = "start")
public class StartKernelCommand implements Callable<Void> {
  @Setter @CommandLine.ParentCommand private KernelCommandSet kernelCommandSet;

  @Override
  public Void call() throws Exception {
    val kernel =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(
                    kernelCommandSet.context.getOptions(), ClassLoader.getSystemClassLoader()))
            .build()
            .kernel();

    kernel.start();
    ((KernelLauncherContext) kernelCommandSet.context).setKernel(kernel);
    KernelLauncher.setKernel(kernel);
    return null;
  }
}
