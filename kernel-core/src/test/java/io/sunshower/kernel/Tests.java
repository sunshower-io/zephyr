package io.sunshower.kernel;

import static io.sunshower.test.common.Tests.relativeToProjectBuild;

import io.sunshower.kernel.module.ModuleEntryWriteProcessor;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.module.phases.*;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import lombok.Getter;
import lombok.val;

public class Tests {

  static final String MODULE_TEMPLATE = "kernel-modules:%s";
  static final String PLUGIN_TEMPLATE = "kernel-tests:test-plugins:%s";

  public static String module(String name) {
    return String.format(PLUGIN_TEMPLATE, name);
  }

  public static InstallationContext resolveModule(String module, KernelProcessContext context)
      throws Exception {

    val ctx = installModule(module, context);
    val process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(new ModuleTransferPhase());
    process.addPhase(new ModuleUnpackPhase());
    process.addPhase(new ModuleInstallationCompletionPhase());
    context.getKernel().getScheduler().registerHandler(ModuleEntryWriteProcessor.getInstance());
    process.call();
    ctx.installedModule =
        context.getContextValue(ModuleInstallationCompletionPhase.INSTALLED_MODULE);
    return ctx;
  }

  public static InstallationContext resolve(String plugin, KernelProcessContext context)
      throws Exception {
    val ctx = install(plugin, context);

    val process = new KernelProcess(context);
    process.addPhase(new ModuleDownloadPhase());
    process.addPhase(new ModuleScanPhase());
    process.addPhase(new ModuleTransferPhase());
    process.addPhase(new ModuleUnpackPhase());
    process.addPhase(new ModuleInstallationCompletionPhase());
    process.call();
    ctx.installedModule =
        context.getContextValue(ModuleInstallationCompletionPhase.INSTALLED_MODULE);
    return ctx;
  }

  public static InstallationContext install(File pluginFile, KernelProcessContext context)
      throws MalformedURLException {
    val kernelFileSystem = context.getKernel().getFileSystem();
    context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    context.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, pluginFile.toURI().toURL());
    context.setContextValue(
        ModuleUnpackPhase.LIBRARY_DIRECTORIES, Collections.singleton("WEB-INF/lib/"));
    val ctx = new InstallationContext();
    ctx.pluginFile = pluginFile;
    ctx.context = context;
    ctx.context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    return ctx;
  }

  public static InstallationContext installModule(String module, KernelProcessContext context)
      throws MalformedURLException {
    val file = relativeToProjectBuild(String.format(MODULE_TEMPLATE, module), "war", "libs");
    return install(file, context);
  }

  public static InstallationContext install(String plugin, KernelProcessContext context)
      throws MalformedURLException {
    val file = relativeToProjectBuild(module(plugin), "war", "libs");
    return install(file, context);
  }

  public static File resolveFile(String plugin, String ext) {
    return relativeToProjectBuild(module(plugin), ext, "libs");
  }

  @Getter
  public static class InstallationContext {
    private File pluginFile;
    private Module installedModule;
    private KernelProcessContext context;

    public Module getInstalledModule() {
      if (installedModule == null) {
        throw new IllegalStateException("Module hasn't been installed yet");
      }
      return installedModule;
    }
  }
}
