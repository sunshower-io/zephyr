package io.sunshower.kernel.core.lifecycle;

import io.sunshower.kernel.concurrency.Context;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.module.KernelModuleEntry;
import lombok.val;

import java.util.List;

public class KernelModuleWritePhase extends Task {
  public KernelModuleWritePhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Context context) {
    val fs = context.get(Kernel.class).getFileSystem();
    if (fs == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }

    List<KernelModuleEntry> entries = context.get(KernelModuleListReadPhase.INSTALLED_MODULE_LIST);
    if (entries == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
