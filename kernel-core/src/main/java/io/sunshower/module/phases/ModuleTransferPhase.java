package io.sunshower.module.phases;

import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

/**
 * This phase transfers a module file from the kernel temp directory to its final destination .
 *
 * <p>This phase is also responsible for the creation of the module filesystem
 */
public class ModuleTransferPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {

  public static final String MODULE_ASSEMBLY_FILE = "MODULE_ASSEMBLY";
  public static final String MODULE_DIRECTORY = "MODULE_DIRECTORY";
  public static final String MODULE_FILE_SYSTEM = "MODULE_FILE_SYSTEM";

  static final Logger log = Logging.get(ModuleTransferPhase.class);

  static final ResourceBundle bundle;

  static {
    bundle = log.getResourceBundle();
  }

  enum EventType implements KernelProcessEvent {}

  public ModuleTransferPhase() {
    super(EventType.class);
  }

  @Override
  @SuppressWarnings("PMD.PreserveStackTrace")
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    val fs = createFilesystem(context);
    context.setContextValue(MODULE_FILE_SYSTEM, fs);

    val assembly = fs.getPath("module.droplet").toFile();
    File file = context.getContextValue(ModuleDownloadPhase.DOWNLOADED_FILE);
    val parent = assembly.getParentFile();
    if (!assembly.exists()) {
      if (!(parent.exists() || parent.mkdirs())) {
        log.log(Level.WARNING, "transfer.file.makedirectory", parent);
      }
    }
    context.setContextValue(MODULE_DIRECTORY, parent);

    log.log(Level.INFO, "transfer.file.beginning", new Object[] {file, assembly});
    try {
      Files.copy(file.toPath(), assembly.toPath(), StandardCopyOption.REPLACE_EXISTING);
      context.setContextValue(MODULE_ASSEMBLY_FILE, assembly);
      log.log(Level.INFO, "transfer.file.complete", new Object[] {file, assembly});
    } catch (IOException ex) {
      val message =
          MessageFormat.format(
              bundle.getString("transfer.file.failed"), assembly, file, ex.getMessage());
      log.log(Level.WARNING, message);
      val pex = new PhaseException(State.Unrecoverable, this, message);
      pex.addSuppressed(ex);
      throw pex;
    }
  }

  private FileSystem createFilesystem(KernelProcessContext context) {
    ModuleDescriptor descriptor = context.getContextValue(ModuleScanPhase.MODULE_DESCRIPTOR);
    val coordinate = descriptor.getCoordinate();
    val uri =
        String.format(
            "droplet://%s.%s?version=%s",
            coordinate.getGroup(), coordinate.getName(), coordinate.getVersion());
    log.log(Level.INFO, "transfer.uri", uri);

    try {
      val fs = FileSystems.newFileSystem(URI.create(uri), Collections.emptyMap());

      log.log(
          Level.INFO,
          "transfer.uri.success",
          new Object[] {uri, fs.getRootDirectories().iterator().next()});
      return fs;
    } catch (IOException ex) {
      log.log(Level.WARNING, "transfer.uri.failure", ex.getMessage());
      log.log(Level.FINE, "Error", ex);
      throw new PhaseException(State.Unrecoverable, this, ex);
    }
  }
}
