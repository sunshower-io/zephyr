package io.sunshower.kernel.core;

public class KernelStartProcess {
  //  static final String channel = "kernel:lifecycle:process:start";
  //
  //  final Kernel kernel;
  //  final SunshowerKernel.DefaultLifecycle lifecycle;
  //
  //  public KernelStartProcess(Kernel kernel, SunshowerKernel.DefaultLifecycle lifecycle) {
  //    this.kernel = kernel;
  //    this.lifecycle = lifecycle;
  //    lifecycle.state = KernelLifecycle.State.Stopping;
  //  }
  //
  //  @Override
  //  public String getChannel() {
  //    return channel;
  //  }
  //
  //  @Override
  //  public void process(ConcurrentProcess process) {
  //    process.perform();
  //  }
  //
  //  @Override
  //  public void perform() {
  //    val process = createProcess();
  //    try {
  //      process.call();
  //      lifecycle.state = KernelLifecycle.State.Running;
  //    } catch (Exception e) {
  //      throw new KernelException(e);
  //    } finally {
  //      kernel.getScheduler().unregisterHandler(this);
  //    }
  //  }
  //
  //  private KernelProcess createProcess() {
  //    val context = new KernelProcessContext(kernel);
  //    val process = new KernelProcess(context);
  //    process.addPhase(new KernelFileSystemCreatePhase());
  //    process.addPhase(new KernelModuleListReadPhase());
  //    process.addPhase(new KernelClassLoaderCreationPhase());
  //
  //    return process;
  //  }
}
