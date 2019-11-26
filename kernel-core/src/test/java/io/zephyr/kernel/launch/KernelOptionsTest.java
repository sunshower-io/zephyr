package io.zephyr.kernel.launch;

import io.sunshower.test.common.Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KernelOptionsTest {

  private KernelOptions options;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    System.setProperty("sunshower.home", Tests.createTemp().getAbsolutePath());
  }

  @Test
  void ensureHomeDirectoryWorks() {
    options.validate();
  }
}
