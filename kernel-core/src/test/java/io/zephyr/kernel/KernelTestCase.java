package io.zephyr.kernel;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.DaggerSunshowerKernelConfiguration;
import io.zephyr.kernel.core.Kernel;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.core.SunshowerKernelConfiguration;
import io.zephyr.kernel.launch.KernelOptions;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;

@Log
public class KernelTestCase {

  protected Kernel kernel;
  protected File yamlModule;
  protected File springPlugin;
  protected SunshowerKernelConfiguration cfg;

  @BeforeEach
  protected void setUp() {
    val options = new KernelOptions();
    options.setHomeDirectory(Tests.createTemp());
    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.factory()
            .create(options, ClassLoader.getSystemClassLoader());
    kernel = cfg.kernel();
  }

  @AfterEach
  protected void tearDown() throws IOException {
    try {
      FileSystems.getFileSystem(URI.create("droplet://kernel")).close();
    } catch (Exception ex) {
      log.info(ex.getMessage());
    }
  }
}
