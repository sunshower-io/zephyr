package io.zephyr.kernel.core.actions;

import io.sunshower.gyre.Scope;
import io.zephyr.kernel.Assembly;
import io.zephyr.kernel.concurrency.Task;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.events.Events;
import io.zephyr.kernel.extensions.ModuleAssemblyExtractor;
import io.zephyr.kernel.log.Logging;
import io.zephyr.kernel.status.StatusType;
import java.io.File;
import java.nio.file.FileSystem;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
  @SuppressWarnings({
    "PMD.CloseResource",
    "PMD.DataflowAnomalyAnalysis",
    "PMD.AvoidInstantiatingObjectsInLoops"
  })
  public TaskValue run(Scope context) {

    File assemblyFile = context.get(ModuleTransferPhase.MODULE_ASSEMBLY_FILE);
    val assembly = new Assembly(assemblyFile);

    fireExtractionInitiated(assembly);

    FileSystem moduleFileSystem = context.get(ModuleTransferPhase.MODULE_FILE_SYSTEM);

    val extractors =
        ServiceLoader.load(ModuleAssemblyExtractor.class, kernel.getClassLoader()).stream()
            .map(Provider::get)
            .sorted()
            .collect(Collectors.toList());

    var anyworked = false;
    for (val extractor : extractors) {
      try {
        log.log(Level.INFO, "module.extractor.beforeapplication", extractor);
        extractor.extract(assembly, moduleFileSystem, this);
        log.log(Level.INFO, "module.extractor.afterapplication", extractor);
        anyworked = true;
      } catch (Exception | ServiceConfigurationError ex) {
        log.log(Level.INFO, "module.extractor.error", new Object[] {ex.getMessage(), extractor});
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.FINE, "module.extractor.actualerror", ex);
        }
        fireExtractorFailed(extractor, ex);
      }
    }

    if (anyworked) {
      fireExtractionCompleted(assembly);
    } else {
      fireNoValidExtractors(assembly);
    }
    context.set(MODULE_ASSEMBLY, assembly);
    return null;
  }

  private void fireExtractionCompleted(Assembly assembly) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_ASSEMBLY_EXTRACTION_COMPLETED,
        Events.create(
            assembly, StatusType.PROGRESSING.resolvable("Successfully created module assembly")));
  }

  private void fireExtractionInitiated(Assembly assembly) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_ASSEMBLY_EXTRACTION_INITIATED, Events.create(assembly));
  }

  private void fireExtractorFailed(ModuleAssemblyExtractor extractor, Throwable ex) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_ASSEMBLY_EXTRACTION_FAILED,
        Events.create(extractor, StatusType.FAILED.resolvable(ex.getMessage())));
  }

  private void fireNoValidExtractors(Assembly assembly) {
    kernel.dispatchEvent(
        ModulePhaseEvents.MODULE_ASSEMBLY_EXTRACTION_FAILED,
        Events.create(assembly, StatusType.FAILED.unresolvable("no valid extractors were found")));
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
