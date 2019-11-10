package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.module.KernelModuleEntry;
import io.sunshower.kernel.module.ModuleListParser;
import lombok.val;

public class KernelModuleListReadPhase extends Task {

  public static final String INSTALLED_MODULE_LIST = "MODULE_LIST_INSTALLED";

  public KernelModuleListReadPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    val fs = scope.<SunshowerKernel>get("SunshowerKernel").getFileSystem();

    if (fs == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
    val entries = ModuleListParser.read(fs, KernelModuleEntry.MODULE_LIST);
    scope.set(INSTALLED_MODULE_LIST, entries);
    return null;
  }
}
