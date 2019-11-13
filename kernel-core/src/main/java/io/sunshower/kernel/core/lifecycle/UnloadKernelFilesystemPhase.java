package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;

import java.util.logging.Logger;

public class UnloadKernelFilesystemPhase extends Task {
  static final Logger logger = Logger.getLogger(UnloadKernelFilesystemPhase.class.getName());

  public UnloadKernelFilesystemPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    try {
      logger.info("unloading kernel filesystem");
      scope.<Kernel>get("SunshowerKernel").getFileSystem().close();
      logger.info("kernel filesystem unloaded");
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
