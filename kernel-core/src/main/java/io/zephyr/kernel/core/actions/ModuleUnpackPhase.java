package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.extensions.ModuleAssemblyExtractor;
import io.zephyr.kernel.log.Logging;
import java.io.File;
import java.nio.file.FileSystem;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings("PMD.UnusedPrivateMethod")
public class ModuleUnpackPhase extends Task implements ModuleAssemblyExtractor.ExtractionListener {

  static final Logger log;
  static final ResourceBundle bundle;
  public static final String MODULE_ASSEMBLY = "MODULE_RELEVANT_SEARCH_PATHS";

  static {
    log = Logging.get(ModuleUnpackPhase.class);
    bundle = log.getResourceBundle();
  }

  private final Kernel kernel;

  public ModuleUnpackPhase(String name, Kernel kernel) {
    super(name);
    this.kernel = kernel;
  }

  @Override
  @SuppressWarnings({"PMD.CloseResource", "PMD.DataflowAnomalyAnalysis"})
  public TaskValue run(Scope context) {
    File assemblyFile = context.get(ModuleTransferPhase.MODULE_ASSEMBLY_FILE);
    val assembly = new Assembly(assemblyFile);
    FileSystem moduleFileSystem = context.get(ModuleTransferPhase.MODULE_FILE_SYSTEM);

    val extractors = ServiceLoader.load(ModuleAssemblyExtractor.class, kernel.getClassLoader());

    for (val extractor : extractors) {
      try {
        log.log(Level.INFO, "module.extractor.beforeapplication", extractor);
        extractor.extract(assembly, moduleFileSystem, this);
        log.log(Level.INFO, "module.extractor.afterapplication", extractor);
      } catch (Exception ex) {
        log.log(Level.INFO, "module.extractor.error", extractor);
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.FINE, "module.extractor.actualerror", ex);
        }
      }
    }
    context.set(MODULE_ASSEMBLY, assembly);
    return null;
  }

  @Override
  public void beforeEntryExtracted(String name, Object target) {
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "module.unpack.file", new Object[] {name, target});
    }
  }

  @Override
  public void afterEntryExtracted(String name, Object target) {
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "module.unpack.file.complete", new Object[] {name, target});
    }
  }
}
