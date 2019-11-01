package io.sunshower.kernel.shell.commands;

import io.sunshower.kernel.core.DaggerSunshowerKernelConfiguration;
import io.sunshower.kernel.core.SunshowerKernelInjectionModule;
import io.sunshower.kernel.shell.KernelLauncherContext;
import java.util.concurrent.Callable;
import lombok.Setter;
import lombok.val;
import picocli.CommandLine;

@SuppressWarnings("PMD.DoNotUseThreads")
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
    kernel.getScheduler().synchronize();
    ((KernelLauncherContext) kernelCommandSet.context).setKernel(kernel);
    return null;
  }
}
