package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.module.KernelModuleEntry;
import java.util.List;
import lombok.val;

public class KernelModuleWritePhase extends Task {
  public KernelModuleWritePhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    val fs = scope.<Kernel>get("SunshowerKernel").getFileSystem();
    if (fs == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }

    List<KernelModuleEntry> entries = scope.get(KernelModuleListReadPhase.INSTALLED_MODULE_LIST);
    if (entries == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
