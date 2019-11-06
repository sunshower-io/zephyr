package io.sunshower.kernel.lifecycle.processes;

import io.sunshower.kernel.concurrency.Context;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.core.lifecycle.KernelFileSystemCreatePhase;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.logging.Level;
import lombok.val;

public class KernelFilesystemCreatePhase implements Task {

  static final Logger log = Logging.get(KernelFileSystemCreatePhase.class, "KernelLifecycle");
  final Object lock = new Object();
  static final String FILE_SYSTEM_ROOT = "droplet://kernel";

  @Override
  public TaskValue run(Context context) {
    try {
      log.log(Level.INFO, "kernel.lifecycle.filesystem.init");

      val kernel = context.get(SunshowerKernel.class);
      val fs = FileSystems.newFileSystem(URI.create(FILE_SYSTEM_ROOT), Collections.emptyMap());
      kernel.setFileSystem(fs);
      log.log(Level.INFO, "kernel.lifecycle.filesystem.created", fs.getRootDirectories());
    } catch (Exception ex) {
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }
}
