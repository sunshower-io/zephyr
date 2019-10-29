package io.sunshower.module.phases;

import static io.sunshower.test.common.Tests.relativeToProjectBuild;

import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.DaggerSunshowerKernelConfiguration;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.core.SunshowerKernelInjectionModule;
import io.sunshower.kernel.dependencies.DependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.process.KernelProcess;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractModulePhaseTestCase {
  static final String MODULE_TEMPLATE = "kernel-modules:%s";
  static final String PLUGIN_TEMPLATE = "kernel-tests:test-plugins:%s";

  protected File sunshowerHome;
  protected KernelOptions options;
  protected SunshowerKernel kernel;
  protected KernelProcessContext context;
  protected FileSystem kernelFileSystem;
  protected DependencyGraph dependencyGraph;

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

  @BeforeEach
  void setUp() throws Exception {
    sunshowerHome = Tests.createTemp(".sunshower-home");
    options = new KernelOptions();
    options.setHomeDirectory(sunshowerHome);

    val injectionModule =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();

    kernel = (SunshowerKernel) injectionModule.kernel();
    dependencyGraph = injectionModule.dependencyGraph();
    context = new KernelProcessContext(kernel);

    SunshowerKernel.setKernelOptions(options);
    kernelFileSystem =
        FileSystems.newFileSystem(URI.create("droplet://deploy"), Collections.emptyMap());
  }

  protected String module(String name) {
    return String.format(PLUGIN_TEMPLATE, name);
  }

  protected InstallationContext resolveModule(String module) throws Exception {

    val ctx = installModule(module);
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

  protected InstallationContext resolve(String plugin) throws Exception {
    val ctx = install(plugin);

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

  protected InstallationContext install(File pluginFile) throws MalformedURLException {
    context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    context.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, pluginFile.toURI().toURL());
    context.setContextValue(
        ModuleUnpackPhase.LIBRARY_DIRECTORIES, Collections.singleton("WEB-INF/lib/"));
    val context = new InstallationContext();
    context.pluginFile = pluginFile;
    context.context = this.context;
    context.context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    return context;
  }

  protected InstallationContext installModule(String module) throws MalformedURLException {
    val file = relativeToProjectBuild(String.format(MODULE_TEMPLATE, module), "war", "libs");
    return install(file);
  }

  protected InstallationContext install(String plugin) throws MalformedURLException {
    val file = relativeToProjectBuild(module(plugin), "war", "libs");
    return install(file);
  }

  protected File resolveFile(String plugin, String ext) {
    return relativeToProjectBuild(module(plugin), ext, "libs");
  }

  @AfterEach
  void tearDown() throws IOException {
    kernelFileSystem.close();
  }
}
