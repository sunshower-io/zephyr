package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;

public class UnloadKernelFilesystemPhase extends Task {
  public UnloadKernelFilesystemPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    try {
      scope.<Kernel>get("SunshowerKernel").getFileSystem().close();
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
