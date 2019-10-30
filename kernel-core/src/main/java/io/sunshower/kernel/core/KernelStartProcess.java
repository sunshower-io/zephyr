package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ConcurrentProcess;
import io.sunshower.kernel.concurrency.Processor;
import io.sunshower.kernel.core.lifecycle.KernelClassLoaderCreationPhase;
import io.sunshower.kernel.core.lifecycle.KernelFileSystemCreatePhase;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class KernelStartProcess implements ConcurrentProcess, Processor {
  static final String channel = "kernel:lifecycle:process:start";
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
    }
    kernel.getScheduler().unregisterHandler(this);
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
