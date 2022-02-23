package io.zephyr.kernel.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ModulePackageConstraintSetTest {

  @Test
  void ensureDeglobbingWorks() {
    assertEquals("hello.world", ModulePackageConstraintSet.deglob("hello.world.*"));
  }

}