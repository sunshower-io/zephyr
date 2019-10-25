package io.sunshower.module.phases;

import static io.sunshower.test.common.Tests.relativeToProjectBuild;

import io.sunshower.kernel.core.DefaultModuleManager;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
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
  static final String PLUGIN_TEMPLATE = "kernel-tests:test-plugins:%s";

  protected File sunshowerHome;
  protected KernelOptions options;
  protected SunshowerKernel kernel;
  protected KernelProcessContext context;
  protected FileSystem kernelFileSystem;

  @Getter
  public static class InstallationContext {
    private File pluginFile;
    private KernelProcessContext context;
  }

  @BeforeEach
  void setUp() throws IOException {
    sunshowerHome = Tests.createTemp(".sunshower-home");
    options = new KernelOptions();
    options.setHomeDirectory(sunshowerHome);
    kernel = new SunshowerKernel(new DefaultModuleManager());
    context = new KernelProcessContext(kernel);

    SunshowerKernel.setKernelOptions(options);
    kernelFileSystem =
        FileSystems.newFileSystem(URI.create("droplet://deploy"), Collections.emptyMap());
  }

  protected String module(String name) {
    return String.format(PLUGIN_TEMPLATE, name);
  }

  protected InstallationContext install(File pluginFile) throws MalformedURLException {
    context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    context.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, pluginFile.toURI().toURL());
    val context = new InstallationContext();
    context.pluginFile = pluginFile;
    context.context = this.context;
    context.context.setContextValue(
        ModuleDownloadPhase.TARGET_DIRECTORY, kernelFileSystem.getPath("modules"));
    return context;
  }

  protected InstallationContext install(String plugin) throws MalformedURLException {
    val file = relativeToProjectBuild(module(plugin), "war", "libs");
    return install(file);
  }

  @AfterEach
  void tearDown() throws IOException {
    kernelFileSystem.close();
  }
}
