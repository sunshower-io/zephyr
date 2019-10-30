package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class SunshowerKernelTest {

  private Kernel kernel;
  private SunshowerKernelConfiguration cfg;

  @BeforeEach
  void setUp() {

    val logger = Logger.getGlobal();
    logger.addHandler(new ConsoleHandler());
    logger.setLevel(Level.ALL);

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
  void ensureStartingKernelWorks() throws IOException {
    try {
      assertNull(kernel.getFileSystem(), "kernel filesystem must initially be null");
      kernel.start();
      kernel.getScheduler().await(KernelStartProcess.channel);
      assertNotNull(kernel.getFileSystem(), "kernel filesystem must now be set");
    } finally {
      kernel.getFileSystem().close();
    }
  }
}
