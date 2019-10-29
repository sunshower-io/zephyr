package io.sunshower.module.phases;

import static java.text.MessageFormat.format;

import io.sunshower.kernel.core.ModuleDescriptor;
import io.sunshower.kernel.core.ModuleScanner;
import io.sunshower.kernel.log.Logger;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.process.*;
import io.sunshower.kernel.process.Process;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import lombok.val;

/**
 * This phase runs after ModuleDownloadPhase and
 *
 * <p>1. Loads all loaded ModuleScanners from the kernel 2. Finds a scanner that can handle the
 * current file-type 3. Applies that scanner to the file to produce a Coordinate 4. Puts that
 * coordinate into the context for further processing
 */
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.UseVarargs"})
public class ModuleScanPhase extends AbstractPhase<KernelProcessEvent, KernelProcessContext> {
  public static final String MODULE_DESCRIPTOR = "MODULE_SCAN_MODULE_DESCRIPTOR";

  enum EventType implements KernelProcessEvent {}

  static final ResourceBundle bundle;
  static final Logger logger = Logging.get(ModuleScanPhase.class);

  static {
    bundle = logger.getResourceBundle();
  }

  public ModuleScanPhase() {
    super(EventType.class);
  }

  @Override
  protected void doExecute(
      Process<KernelProcessEvent, KernelProcessContext> process, KernelProcessContext context) {
    File downloaded = context.getContextValue(ModuleDownloadPhase.DOWNLOADED_FILE);
    context.setContextValue(ModuleScanPhase.MODULE_DESCRIPTOR, scan(downloaded, context));
  }

  private ModuleDescriptor scan(File downloaded, KernelProcessContext context) {
    val scanners = context.getKernel().locateServices(ModuleScanner.class);
    URL url = context.getContextValue(ModuleDownloadPhase.DOWNLOAD_URL);
    if (scanners.isEmpty()) {
      throw new PhaseException(
          State.Error, this, format(bundle.getString("module.scan.noscanners")));
    }
    val descriptor =
        scanners.stream().map(t -> t.scan(downloaded, url)).flatMap(Optional::stream).findAny();
    if (descriptor.isPresent()) {
      return descriptor.get();
    } else {
      throw new PhaseException(
          State.Unrecoverable,
          this,
          format(bundle.getString("module.scan.nosuitablescanners"), downloaded));
    }
  }
}
