package io.zephyr.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.lang.events.Events;
import io.zephyr.kernel.KernelModuleEntry;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.KernelEventTypes;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.module.ModuleListParser;
import lombok.val;

public class KernelModuleListReadPhase extends Task {

  public static final String INSTALLED_MODULE_LIST = "MODULE_LIST_INSTALLED";

  public KernelModuleListReadPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    val kernel = scope.<SunshowerKernel>get("SunshowerKernel");
    val fs = kernel.getFileSystem();

    if (fs == null) {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
    val entries = ModuleListParser.read(fs, KernelModuleEntry.MODULE_LIST);
    scope.set(INSTALLED_MODULE_LIST, entries);
    kernel.dispatchEvent(KernelEventTypes.KERNEL_MODULE_LIST_READ, Events.create(kernel));
    return null;
  }
}
