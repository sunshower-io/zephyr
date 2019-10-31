package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.IOException;

public class UnloadKernelFilesystemPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  public UnloadKernelFilesystemPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    try {
      context.getKernel().getFileSystem().close();
    } catch (IOException e) {
      throw new PhaseException(State.Unrecoverable, this, "failed to close kernel filesystem", e);
    }
  }
}
