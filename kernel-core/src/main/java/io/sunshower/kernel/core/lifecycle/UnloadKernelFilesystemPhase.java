package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnloadKernelFilesystemPhase
    extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  enum EventType implements KernelProcessEvent {}

  static final Logger logger = Logger.getLogger(UnloadKernelFilesystemPhase.class.getName());
  public UnloadKernelFilesystemPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    try {
      logger.info("Unloading filesystem");
      context.getKernel().getFileSystem().close();
      logger.info("Successfully unloaded filesystem");
    } catch (IOException e) {
      logger.log(Level.INFO, "error", e.getMessage());
      logger.log(Level.WARNING, "error", e);

      throw new PhaseException(State.Unrecoverable, this, "failed to close kernel filesystem", e);
    }
  }
}
