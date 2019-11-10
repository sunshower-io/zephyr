package io.sunshower.kernel.core.lifecycle;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.log.Logging;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

public class KernelFilesystemCreatePhase extends Task {

  static final Logger log = Logging.get(KernelFilesystemCreatePhase.class, "KernelLifecycle");
  static final String FILE_SYSTEM_ROOT = "droplet://kernel";

  public KernelFilesystemCreatePhase(String name) {
    super(name);
  }

  @Override
  public TaskValue run(Scope context) {
    try {
      log.log(Level.INFO, "kernel.lifecycle.filesystem.init");

      val kernel = context.<SunshowerKernel>get("SunshowerKernel");
      val fs = FileSystems.newFileSystem(URI.create(FILE_SYSTEM_ROOT), Collections.emptyMap());
      kernel.setFileSystem(fs);
      log.log(Level.INFO, "kernel.lifecycle.filesystem.created", fs.getRootDirectories());
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
