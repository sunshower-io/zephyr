package io.sunshower.kernel.core;

import static io.sunshower.kernel.Tests.resolveModule;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.process.KernelProcessContext;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
public class SunshowerKernelTest {

  private Kernel kernel;
  private SunshowerKernelConfiguration cfg;
  private KernelProcessContext context;

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
    context = new KernelProcessContext(kernel);
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
    try {
      assertNull(kernel.getFileSystem(), "kernel filesystem must initially be null");
      kernel.start();
      kernel.getScheduler().await(KernelStartProcess.channel);
      assertNotNull(kernel.getFileSystem(), "kernel filesystem must now be set");
    } finally {
      kernel.getFileSystem().close();
    }
  }

  @Test
  void ensureStartingKernelProducesClassLoader() throws IOException {

    try {
      assertNull(kernel.getClassLoader(), "kernel filesystem must initially be null");
      kernel.start();
      kernel.getScheduler().await(KernelStartProcess.channel);
      assertNotNull(kernel.getClassLoader(), "kernel filesystem must now be set");
    } finally {
      kernel.getFileSystem().close();
    }
  }

  @Test
  @SuppressWarnings("PMD.UseProperClassLoader")
  void
      ensureInstallingKernelModuleThenStartingKernelResultsInKernelModuleClassesBeingAvailableInClassloader()
          throws Exception {
    kernel.start();
    kernel.getScheduler().synchronize();
    val ctx = resolveModule("sunshower-yaml-reader", context).getInstalledModule();
    kernel.reload();
    kernel.getScheduler().synchronize();
    try {
      val cl = kernel.getClassLoader();
      val clazz =
          Class.forName("io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner", true, cl);
      assertNotEquals(
          clazz.getClassLoader(), getClass().getClassLoader(), "must not be the same classloader");
      kernel.stop();
    } finally {
      ctx.getFileSystem().close();
      kernel.getFileSystem().close();
    }
  }
}
