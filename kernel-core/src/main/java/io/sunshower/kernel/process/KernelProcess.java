package io.sunshower.kernel.process;

public class KernelProcess extends AbstractProcess<KernelProcessEvent, KernelProcessContext> {

  public KernelProcess(KernelProcessContext context) {
    super(KernelProcessEvent.class, context);
  }
}
