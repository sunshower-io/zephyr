package io.sunshower.kernel.module.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.core.*;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.FileSystems;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ModuleLifecycleTest {

  private Kernel kernel;
  private SunshowerKernelConfiguration cfg;

  @BeforeEach
  void setUp() {

    val options = new KernelOptions();
    options.setHomeDirectory(Tests.createTemp("sunshower-kernel-tests"));
    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();
    kernel = cfg.kernel();
    kernel.start();
  }

  @AfterEach
  void tearDown() throws IOException {
    FileSystems.getFileSystem(URI.create("droplet://kernel")).close();
  }

  @Test
  void ensureInstallingKernelModuleWorks() throws MalformedURLException {
    val request = new ModuleInstallationRequest();
    request.setLocation(
        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs")
            .toURI()
            .toURL());
    request.setLifecycleActions(ModuleLifecycle.Actions.Activate);

    val group = new ModuleInstallationGroup(request);
    val action = kernel.getModuleManager().prepare(group);
    assertEquals(action.getInstallationStatuses().size(), 1, "must have one pending action");
  }
}
