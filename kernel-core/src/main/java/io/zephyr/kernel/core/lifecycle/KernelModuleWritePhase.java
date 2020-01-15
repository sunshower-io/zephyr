package io.zephyr.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;
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
