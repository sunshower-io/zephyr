package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Processor;
import io.sunshower.kernel.core.lifecycle.UnloadKernelClassloaderPhase;
import io.sunshower.kernel.core.lifecycle.UnloadKernelFilesystemPhase;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class KernelStopProcess implements ConcurrentProcess, Processor {
  static final String channel = "kernel:lifecycle:process:stop";

  final Kernel kernel;

  @Override
  public String getChannel() {
    return channel;
  }

  @Override
  public void process(ConcurrentProcess process) {
    process.perform();
  }

  @Override
  public void perform() {
    val process = createProcess();
    try {
      process.call();
    } catch (Exception e) {
      throw new KernelException(e);
    } finally {
      kernel.getScheduler().unregisterHandler(this);
    }
  }

  private KernelProcess createProcess() {
    val context = new KernelProcessContext(kernel);
    val proc = new KernelProcess(context);
    proc.addPhase(new UnloadKernelClassloaderPhase());
    proc.addPhase(new UnloadKernelFilesystemPhase());
    return proc;
  }
}
