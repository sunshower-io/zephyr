package io.sunshower.kernel.shell.commands;

import java.util.concurrent.Callable;
import lombok.val;
import picocli.CommandLine;

@CommandLine.Command(name = "stop")
public class KernelStopCommand implements Callable<Void> {

  @CommandLine.ParentCommand private KernelCommandSet kernelCommandSet;

  @Override
  public Void call() throws Exception {
    val kernel = kernelCommandSet.context.getKernel();
    if (kernel == null) {
      kernelCommandSet.context.getConsole().printf("Kernel has not been started");
    } else {
      kernel.stop();
    }
    return null;
  }
}
