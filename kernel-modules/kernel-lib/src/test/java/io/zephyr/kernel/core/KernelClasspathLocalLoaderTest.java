package io.zephyr.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class KernelClasspathLocalLoaderTest {

  @Test
  void ensureGetPackageNameWorks() {
    assertEquals(
        getClass().getPackageName(),
        KernelClasspathLocalLoader.getPackageName(getClass().getName()));
  }
}
