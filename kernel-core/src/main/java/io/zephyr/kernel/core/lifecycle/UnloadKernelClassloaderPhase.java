package io.zephyr.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.classloading.KernelClassloader;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;

@SuppressWarnings("PMD.UseProperClassLoader")
public class UnloadKernelClassloaderPhase extends Task {

  public UnloadKernelClassloaderPhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope scope) {
    try {
      ((KernelClassloader) scope.<Kernel>get("SunshowerKernel").getClassLoader()).close();
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
