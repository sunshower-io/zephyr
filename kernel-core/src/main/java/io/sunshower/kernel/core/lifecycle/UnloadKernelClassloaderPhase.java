package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.classloading.KernelClassloader;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.IOException;
import lombok.val;

public class UnloadKernelClassloaderPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  public UnloadKernelClassloaderPhase() {
    super(EventType.class);
  }

  @Override
  @SuppressWarnings("PMD.UseProperClassLoader")
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    val classloader = (KernelClassloader) context.getKernel().getClassLoader();
    try {
      classloader.close();
    } catch (IOException e) {
      throw new PhaseException(State.Unrecoverable, this, "failed to unload classloader", e);
    }
  }
}
