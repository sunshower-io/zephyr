package io.sunshower.kernel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModuleTest {
  @Test
  void ensureNullParsingThrowsNPE() {
    String a = null;
    assertThrows(
        NullPointerException.class,
        () -> {
          Module.Type.parse(a);
        },
        "null arg must result in NPE");
  }

  @Test
  void ensureParsingKernelModuleHyphenReturnsKernelModule() {
    assertEquals(
        Module.Type.parse("kernel-module"),
        Module.Type.KernelModule,
        "kernel module must parse correctly");
  }

  @Test
  void ensureEmptyStringResultsInIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Module.Type.parse("");
        },
        "invalid module type must throw illegalargumentexception");
  }

  @Test
  void ensurePluginTypeParsesCorrectly() {
    assertEquals(
        Module.Type.parse("plugin"),
        Module.Type.Plugin,
        "module type of plugin must parse to plugin");
  }
}
