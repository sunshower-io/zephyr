package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log
@SuppressFBWarnings
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class SunshowerKernelTest {

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
  }

  @AfterEach
  void tearDown() throws IOException {
    try {
      FileSystems.getFileSystem(URI.create("droplet://kernel")).close();
    } catch (Exception ex) {
      log.info(ex.getMessage());
    }
  }

  @Test
  void ensureKernelIsCreated() {
    kernel = cfg.kernel();
    assertNotNull(kernel, "kernel must not be null");
  }

  @Test
  void ensureInjectionOfPluginManagerWorks() {
    kernel = cfg.kernel();
    assertNotNull(kernel.getModuleManager(), "plugin manager must be injected");
  }

  @Test
  void ensureStartingKernelProducesFileSystem() throws IOException {
    assertNull(kernel.getFileSystem(), "kernel filesystem must initially be null");
    kernel.start();
    assertNotNull(kernel.getFileSystem(), "kernel filesystem must now be set");
  }

  @Test
  void ensureStartingKernelProducesClassLoader() throws IOException {
    assertNull(kernel.getClassLoader(), "kernel filesystem must initially be null");
    kernel.start();
    assertNotNull(kernel.getClassLoader(), "kernel filesystem must now be set");
  }
}
