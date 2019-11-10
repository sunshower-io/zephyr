package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.classloading.KernelClassloader;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;

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
