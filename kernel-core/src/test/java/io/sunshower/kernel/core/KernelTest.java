package io.sunshower.kernel.core;

import io.sunshower.kernel.Kernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.osgi.OsgiEnabledKernel;
import io.sunshower.test.common.Tests;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class KernelTest {

  private Kernel kernel;

  @BeforeEach
  void setUp() {
    val options = new KernelOptions();
    options.setStorage(Tests.createTemp("plugins").getAbsolutePath());
    kernel = new OsgiEnabledKernel(options);
  }

  @Test
  void ensureCopyingWorks() throws MalformedURLException, ExecutionException, InterruptedException {
    val projectfile = Tests.projectOutput("kernel-tests:test-plugins:test-plugin-1", "war");
    //        kernel.install(projectfile.toURI().toURL()).get();
  }
}
