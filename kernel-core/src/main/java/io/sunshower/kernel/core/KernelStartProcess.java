package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Processor;
import io.sunshower.kernel.core.lifecycle.KernelClassLoaderCreationPhase;
import io.sunshower.kernel.core.lifecycle.KernelFileSystemCreatePhase;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import lombok.val;

public class KernelStartProcess implements ConcurrentProcess, Processor {
  static final String channel = "kernel:lifecycle:process:start";

  final Kernel kernel;
  final SunshowerKernel.DefaultLifecycle lifecycle;

  public KernelStartProcess(Kernel kernel, SunshowerKernel.DefaultLifecycle lifecycle) {
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
      lifecycle.state = KernelLifecycle.State.Running;
    } catch (Exception e) {
      throw new KernelException(e);
    } finally {
      kernel.getScheduler().unregisterHandler(this);
    }
  }

  private KernelProcess createProcess() {
    val context = new KernelProcessContext(kernel);
    val process = new KernelProcess(context);
    process.addPhase(new KernelFileSystemCreatePhase());
    process.addPhase(new KernelModuleListReadPhase());
    process.addPhase(new KernelClassLoaderCreationPhase());

    return process;
  }
}
