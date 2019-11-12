package io.sunshower.kernel.core;

import static java.lang.String.format;

import io.sunshower.gyre.*;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.concurrency.Process;
import io.sunshower.kernel.concurrency.TaskBuilder;
import io.sunshower.kernel.concurrency.Tasks;
import io.sunshower.kernel.core.actions.*;
import io.sunshower.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.sunshower.kernel.log.Logging;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;
import io.sunshower.kernel.module.ModuleRequest;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;

@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.AvoidInstantiatingObjectsInLoops"})
final class DefaultModuleInstallationStatusGroup implements ModuleInstallationStatusGroup {

  static final Logger log = Logging.get(DefaultModuleInstallationStatusGroup.class);

  final Kernel kernel;
  private final Process<String> process;
  private final ModuleInstallationGroup installationGroup;

  public DefaultModuleInstallationStatusGroup(ModuleInstallationGroup toInstall, Kernel kernel) {
    this.kernel = kernel;
    this.installationGroup = toInstall;

    val context = Scope.root();
    context.set("SunshowerKernel", kernel);
    context.set(ModuleDownloadPhase.TARGET_DIRECTORY, kernel.getFileSystem().getPath("downloads"));

    context.set(ModuleInstallationCompletionPhase.INSTALLED_PLUGINS, new HashSet<Module>());
    context.set(
        ModuleInstallationCompletionPhase.INSTALLED_KERNEL_MODULES,
        new HashSet<java.lang.Module>());

    val procBuilder = Tasks.newProcess("module:install").withContext(context);
    val taskBuilder =
        procBuilder
            .parallel()
            .coalesce()
            .register(new KernelModuleListReadPhase("module:list:read"));
    addIntermediates(taskBuilder, toInstall);
    this.process = taskBuilder.create();
  }

  private void logProcess(Process<String> process) {
    if (log.isLoggable(Level.FINEST)) {
      log.log(Level.FINEST, "status.primary.installation.process", process.getExecutionGraph());
    }
  }

  @Override
  public CompletionStage<Process<String>> commit() {
    logProcess(process);
    return kernel.getScheduler().submit(process);
  }

  @Override
  public Process<String> getProcess() {
    return process;
  }

  @Override
  public Set<ModuleRequest> getRequests() {
    return new HashSet<>(installationGroup.getRequests());
  }

  private void addIntermediates(TaskBuilder taskBuilder, ModuleInstallationGroup group) {

    /** synchronization point */
    val writeModuleList = "module:kernel:write:list";
    val writeTask = new WriteKernelModuleListPhase(writeModuleList);
    taskBuilder.register(writeTask);

    val writePluginDescriptorName = "module:kernel:install:plugins";
    val writePluginDescriptorPhase = new WritePluginDescriptorPhase(writePluginDescriptorName);
    taskBuilder.register(writePluginDescriptorPhase);
    taskBuilder.task(writePluginDescriptorName).dependsOn(writeModuleList);

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
          .define(Pair.of("INSTALLATION_REQUEST", ModuleInstallationRequest.class), request);
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
  }
}
