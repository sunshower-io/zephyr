package io.sunshower.module.phases;

import io.sunshower.kernel.core.DefaultModuleManager;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractModulePhaseTestCase {

  protected File pluginFile;
  protected File sunshowerHome;
  protected KernelOptions options;
  protected FileSystem fileSystem;
  protected KernelProcessContext context;
  protected ModuleDownloadPhase downloadPhase;
  SunshowerKernel kernel;

  @BeforeEach
  void setUp() throws IOException {
    pluginFile =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");
    sunshowerHome = Tests.createTemp(".sunshower-home");
    options = new KernelOptions();
    options.setHomeDirectory(sunshowerHome);
    SunshowerKernel.setKernelOptions(options);

    kernel = new SunshowerKernel(new DefaultModuleManager());
    fileSystem = FileSystems.newFileSystem(URI.create("droplet://deploy"), Collections.emptyMap());
    context = new KernelProcessContext(kernel);
    context.setContextValue(ModuleDownloadPhase.TARGET_DIRECTORY, fileSystem.getPath("modules"));
    context.setContextValue(ModuleDownloadPhase.DOWNLOAD_URL, pluginFile.toURI().toURL());
  }

  @AfterEach
  void tearDown() throws IOException {
    fileSystem.close();
  }
}
