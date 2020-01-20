package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.api.ModuleEvents;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.Plugins;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.status.StatusType;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

/**
 * This phase transfers a module file from the kernel temp directory to its final destination .
 *
 * <p>This phase is also responsible for the creation of the module filesystem
 */
public class ModuleTransferPhase extends Task {

  public static final String MODULE_ASSEMBLY_FILE = "MODULE_ASSEMBLY";
  public static final String MODULE_DIRECTORY = "MODULE_DIRECTORY";
  public static final String MODULE_FILE_SYSTEM = "MODULE_FILE_SYSTEM";

  static final Logger log = Logging.get(ModuleTransferPhase.class);

  static final ResourceBundle bundle;

  static {
    bundle = log.getResourceBundle();
  }

  public ModuleTransferPhase(String name) {
    super(name);
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  public TaskValue run(Scope scope) {
    val fs = createFilesystem(scope);
    scope.set(MODULE_FILE_SYSTEM, fs);

    val kernel = scope.<Kernel>get("SunshowerKernel");
    val assembly = fs.getPath("module.droplet").toFile();
    File file = scope.get(ModuleDownloadPhase.DOWNLOADED_FILE);
    val parent = assembly.getParentFile();
    if (!assembly.exists()) {
      if (!(parent.exists() || parent.mkdirs())) {
        log.log(Level.WARNING, "transfer.file.makedirectory", parent);
      }
    }
    scope.set(MODULE_DIRECTORY, parent);

    log.log(Level.INFO, "transfer.file.beginning", new Object[] {file, assembly});
    try {
      Files.copy(file.toPath(), assembly.toPath(), StandardCopyOption.REPLACE_EXISTING);
      scope.set(MODULE_ASSEMBLY_FILE, assembly);
      log.log(Level.INFO, "transfer.file.complete", new Object[] {file, assembly});
    } catch (IOException ex) {
      val message =
          MessageFormat.format(
              bundle.getString("transfer.file.failed"), assembly, file, ex.getMessage());
      log.log(Level.WARNING, message);
      kernel.dispatchEvent(
          ModuleEvents.INSTALL_FAILED,
          Events.createWithStatus(
              StatusType.FAILED.unresolvable("Failed to transfer module.  Reason: " + message)));
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private FileSystem createFilesystem(Scope scope) {
    ModuleDescriptor descriptor = scope.get(ModuleScanPhase.MODULE_DESCRIPTOR);
    val coordinate = descriptor.getCoordinate();
    try {
      val result = Plugins.getFileSystem(coordinate);
      val uri = result.fst;
      val fs = result.snd;
      log.log(Level.INFO, "transfer.uri", uri);

      log.log(
          Level.INFO,
          "transfer.uri.success",
          new Object[] {uri, fs.getRootDirectories().iterator().next()});
      return fs;
    } catch (IOException ex) {
      log.log(Level.WARNING, "transfer.uri.failure", ex.getMessage());
      log.log(Level.FINE, "Error", ex);
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
  }
}
