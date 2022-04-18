package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.common.io.Files;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.concurrency.TaskException;
import io.zephyr.kernel.concurrency.TaskStatus;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.ModuleDescriptor;
import io.zephyr.kernel.core.Modules;
import io.zephyr.kernel.events.KernelEvents;
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
@SuppressWarnings("PMD.UnusedPrivateMethod")
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

    val kernel = scope.<Kernel>get("SunshowerKernel");
    moduleTransferInitiated(kernel);
    val fs = createFilesystem(kernel, scope);

    scope.set(MODULE_FILE_SYSTEM, fs);

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
      Files.transferTo(file, assembly);
      //      java.nio.file.Files.copy(file.toPath(), assembly.toPath(),
      // StandardCopyOption.REPLACE_EXISTING);
      scope.set(MODULE_ASSEMBLY_FILE, assembly);
      log.log(Level.INFO, "transfer.file.complete", new Object[] {file, assembly});
      dispatchEvent(kernel, fs, ModulePhaseEvents.MODULE_TRANSFER_COMPLETED);
    } catch (Exception ex) {
      val message =
          MessageFormat.format(
              bundle.getString("transfer.file.failed"), assembly, file, ex.getMessage());
      log.log(Level.WARNING, message);
      dispatchTransferFailed(kernel, message);
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
    return null;
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private FileSystem createFilesystem(Kernel kernel, Scope scope) {
    ModuleDescriptor descriptor = scope.get(ModuleScanPhase.MODULE_DESCRIPTOR);
    dispatchEvent(kernel, descriptor, ModulePhaseEvents.MODULE_FILESYSTEM_CREATION_INITIATED);
    val coordinate = descriptor.getCoordinate();
    try {
      val result = Modules.getFileSystem(coordinate, kernel);
      val uri = result.fst;
      val fs = result.snd;
      log.log(Level.INFO, "transfer.uri", uri);

      log.log(
          Level.INFO,
          "transfer.uri.success",
          new Object[] {uri, fs.getRootDirectories().iterator().next()});
      dispatchEvent(kernel, fs, ModulePhaseEvents.MODULE_FILESYSTEM_CREATION_COMPLETED);

      return fs;
    } catch (IOException ex) {
      handleFilesystemCreationFailed(kernel, ex);
      throw new TaskException(ex, TaskStatus.UNRECOVERABLE);
    }
  }

  private void handleFilesystemCreationFailed(Kernel kernel, IOException ex) {
    log.log(Level.WARNING, "transfer.uri.failure", ex.getMessage());
    log.log(Level.FINE, "Error", ex);
    dispatchEvent(kernel, ex, ModulePhaseEvents.MODULE_FILESYSTEM_CREATION_FAILED);
    dispatchTransferFailed(kernel, ex.getMessage());
  }

  private void dispatchEvent(Kernel kernel, Object value, ModulePhaseEvents eventType) {
    kernel.dispatchEvent(
        eventType,
        KernelEvents.create(
            value,
            StatusType.PROGRESSING.resolvable(String.format("%s : %s", value, eventType.name()))));
  }

  private void moduleTransferInitiated(Kernel kernel) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_TRANSFER_INITIATED,
        KernelEvents.createWithStatus(
            StatusType.PROGRESSING.resolvable("Scanning module assembly")));
  }

  private void dispatchTransferFailed(Kernel kernel, String message) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_TRANSFER_FAILED,
        KernelEvents.createWithStatus(
            StatusType.FAILED.unresolvable("Failed to transfer module.  Reason: " + message)));
  }
}
