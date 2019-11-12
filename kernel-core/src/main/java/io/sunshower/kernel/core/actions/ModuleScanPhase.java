package io.sunshower.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.Task;
import io.sunshower.kernel.concurrency.TaskException;
import io.sunshower.kernel.concurrency.TaskStatus;
import io.sunshower.kernel.core.Kernel;
import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.core.ModuleScanner;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import lombok.val;

/**
 * This phase runs after ModuleDownloadPhase and
 *
 * <p>1. Loads all loaded ModuleScanners from the kernel 2. Finds a scanner that can handle the
 * current file-type 3. Applies that scanner to the file to produce a Coordinate 4. Puts that
 * coordinate into the context for further processing
 */
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.UseVarargs"})
public class ModuleScanPhase extends Task {
  public static final String MODULE_DESCRIPTOR = "MODULE_SCAN_MODULE_DESCRIPTOR";
  public static final String SCANNED_PLUGINS = "SCANNNED_PLUGINS";
  public static final String SCANNED_KERNEL_MODULES = "SCANNED_KERNEL_MODULES";

  static final ResourceBundle bundle;
  static final Logger logger = Logging.get(ModuleScanPhase.class);

  static {
    bundle = logger.getResourceBundle();
  }

  public ModuleScanPhase(String name) {
    super(name);
  }

  private ModuleDescriptor scan(File downloaded, Scope context) {
    val scanners = context.<Kernel>get("SunshowerKernel").locateServices(ModuleScanner.class);
    URL url = (URL) parameters().get(ModuleDownloadPhase.DOWNLOAD_URL);
    if (scanners.isEmpty()) {
      // todo: add error message
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
    val descriptor =
        scanners.stream().map(t -> t.scan(downloaded, url)).flatMap(Optional::stream).findAny();
    if (descriptor.isPresent()) {
      val request = (ModuleInstallationRequest) parameters().get(ModuleInstallationRequest.class);
      val result = descriptor.get();
      request.setCoordinate(result.getCoordinate());
      return result;
    } else {
      throw new TaskException(TaskStatus.UNRECOVERABLE);
    }
  }

  @Override
  public TaskValue run(Scope context) {
    File downloaded = context.get(ModuleDownloadPhase.DOWNLOADED_FILE);
    val result = scan(downloaded, context);
    context.set(ModuleScanPhase.MODULE_DESCRIPTOR, result);

    if (result.getType() == Module.Type.KernelModule) {
      context.computeIfAbsent(ModuleScanPhase.SCANNED_KERNEL_MODULES, new HashSet<>()).add(result);
    } else {
      context.computeIfAbsent(ModuleScanPhase.SCANNED_PLUGINS, new HashSet<>()).add(result);
    }
    return null;
  }
}
