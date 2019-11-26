package io.zephyr.kernel.launch;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.test.common.Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KernelOptionsTest {

  private KernelOptions options;
  private String file;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    file = Tests.createTemp().getAbsolutePath();
    System.setProperty("sunshower.home", file);
  }

  @Test
  void ensureHomeDirectoryWorks() {
    options.validate();
    assertEquals(options.getHomeDirectory().getAbsolutePath(), file, "must be same");
  }
}
