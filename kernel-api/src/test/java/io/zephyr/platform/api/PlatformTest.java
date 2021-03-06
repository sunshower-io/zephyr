package io.zephyr.platform.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class PlatformTest {

  @Test
  @EnabledOnOs({OS.LINUX})
  void ensureCurrentPlatformIsLinux() {
    assertTrue(Platform.OperatingSystem.is(Platform.OperatingSystem.Linux));
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void ensureCurrentPlatformIsWindows() {
    assertTrue(Platform.OperatingSystem.is(Platform.OperatingSystem.Windows));
  }

  @Test
  @EnabledOnOs(OS.MAC)
  void ensureCurrentPlatformIsOSX() {
    assertTrue(Platform.OperatingSystem.is(Platform.OperatingSystem.OSX));
  }
}
