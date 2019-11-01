package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Processor;
import io.sunshower.kernel.core.lifecycle.UnloadKernelClassloaderPhase;
import io.sunshower.kernel.core.lifecycle.UnloadKernelFilesystemPhase;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import lombok.val;

public class KernelStopProcess implements ConcurrentProcess, Processor {
  static final String channel = "kernel:lifecycle:process:stop";

  final Kernel kernel;
  final SunshowerKernel.DefaultLifecycle lifecycle;

  public KernelStopProcess(Kernel kernel, SunshowerKernel.DefaultLifecycle lifecycle) {
    this.kernel = kernel;
    this.lifecycle = lifecycle;
    lifecycle.state = KernelLifecycle.State.Stopping;
  }

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
      lifecycle.state = KernelLifecycle.State.Stopped;
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
