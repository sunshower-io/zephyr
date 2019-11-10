package io.sunshower.kernel.core;

import static java.lang.String.format;

import io.sunshower.gyre.Pair;
import io.sunshower.gyre.Scope;
import io.sunshower.kernel.concurrency.*;
import io.sunshower.kernel.core.actions.*;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import lombok.val;

@SuppressWarnings({
  "PMD.FinalizeOverloaded",
  "PMD.UnusedPrivateMethod",
  "PMD.UnusedFormalParameter",
  "PMD.AvoidInstantiatingObjectsInLoops"
})
public class DefaultModuleManager implements ModuleManager {

  static final Logger log = Logging.get(DefaultModuleManager.class, "KernelMember");

  private Kernel kernel;
  final DependencyGraph dependencyGraph;

  @Inject
  public DefaultModuleManager(DependencyGraph graph) {
    this.dependencyGraph = graph;
  }

  @Override
  public ModuleInstallationStatusGroup prepare(ModuleInstallationGroup group) {
    check();
    val context = Scope.root();
    context.set("SunshowerKernel", kernel);
    context.set(ModuleDownloadPhase.TARGET_DIRECTORY, kernel.getFileSystem().getPath("downloads"));
    val procBuilder = Tasks.newProcess("module:install").withContext(context);
    val taskBuilder =
        procBuilder
            .parallel()
            .coalesce()
            .register(new KernelModuleListReadPhase("module:list:read"));

    val status = new ModuleInstallationStatusGroup();
    addIntermediates(taskBuilder, group, status, context);
    return status;
  }

  private void addIntermediates(
      TaskBuilder taskBuilder,
      ModuleInstallationGroup group,
      ModuleInstallationStatusGroup status,
      Scope context) {

    /** synchronization point */
    val writeModuleList = "module:kernel:write:list";
    val writeTask = new WriteKernelModuleListPhase(writeModuleList);
    taskBuilder.register(writeTask);
    val requests = group.getModules();

    for (val request : requests) {

      /** download modules */
      val location = request.getLocation();
      val name = format("module:download:%s", location);
      val task = new ModuleDownloadPhase(name);
      task.parameters()
          .define(Pair.of(ModuleDownloadPhase.DOWNLOAD_URL, URL.class), request.getLocation());
      taskBuilder.register(task);
      taskBuilder.task(name).dependsOn("module:list:read");

      /** scan modules */
      val moduleName = format("module:scan:%s", location);
      val scanTask = new ModuleScanPhase(moduleName);
      scanTask
          .parameters()
          .define(Pair.of(ModuleDownloadPhase.DOWNLOAD_URL, URL.class), request.getLocation());
      taskBuilder.register(scanTask);
      taskBuilder.task(moduleName).dependsOn(name);

      /** transfer modules */
      val transferModuleName = format("module:transfer:%s", location);
      val transferTask = new ModuleTransferPhase(transferModuleName);
      taskBuilder.register(transferTask);
      taskBuilder.task(transferModuleName).dependsOn(moduleName);

      /** unpack modules */
      val unpackModuleName = format("module:unpack:%s", location);
      val unpackTask = new ModuleUnpackPhase(unpackModuleName);
      taskBuilder.register(unpackTask);
      taskBuilder.task(unpackModuleName).dependsOn(transferModuleName);

      /** create and install module */
      val installModuleName = format("module:install:%s", location);
      val installTask = new ModuleInstallationCompletionPhase(installModuleName);
      taskBuilder.register(installTask);
      taskBuilder.task(installModuleName).dependsOn(unpackModuleName);

      /** Write kernel list */
      taskBuilder.task(writeModuleList).dependsOn(installModuleName);
    }
    status.setProcess(taskBuilder.create());
  }

  @Override
  public void initialize(Kernel kernel) {
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.initialize", new Object[] {this, kernel});
    }
    if (kernel == null) {
      throw new IllegalStateException("cannot initialize with null kernel");
    }
    if (log.isLoggable(Level.INFO)) {
      log.log(Level.INFO, "member.modulemanager.complete", new Object[] {this, kernel});
    }
    this.kernel = kernel;
  }

  @Override
  public void finalize(Kernel kernel) {}

  private void check() {
    if (kernel == null) {
      throw new IllegalStateException(
          "Error: module download manager has not been properly initialized");
    }
  }
}
