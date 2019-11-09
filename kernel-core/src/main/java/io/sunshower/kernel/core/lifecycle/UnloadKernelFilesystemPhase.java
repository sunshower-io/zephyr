package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.concurrency.Context;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;

public class UnloadKernelFilesystemPhase extends Task {
  public UnloadKernelFilesystemPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Context context) {
    try {
      context.get(Kernel.class).getFileSystem().close();
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
