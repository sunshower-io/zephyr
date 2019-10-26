package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.ModuleLifecycle;
import io.sunshower.kernel.dependencies.UnsatisfiedDependencyException;
import io.sunshower.module.phases.AbstractModulePhaseTestCase;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultModuleManagerTest extends AbstractModulePhaseTestCase {
  @Test
  void ensureInstallingAndResolvingModuleWithNoDependenciesWorks() throws Exception {
    val ctx = resolve("test-plugin-1");
    val mod = ctx.getInstalledModule();

    assertEquals(
        "Module must be installed", mod.getLifecycle().getState(), ModuleLifecycle.State.Installed);

    kernel.getModuleManager().install(mod);
    kernel.getModuleManager().resolve(mod);

    assertEquals(
        "Module must be resolved", mod.getLifecycle().getState(), ModuleLifecycle.State.Resolved);
  }

  @Test
  @DisplayName("modules must be installable but not resolvable")
  void ensureInstallingModuleWithoutDependencySucceeds() throws Exception {
    val ctx = resolve("test-plugin-2");
    val mod = ctx.getInstalledModule();
    assertEquals(
        "Module must be installed", mod.getLifecycle().getState(), ModuleLifecycle.State.Installed);

    kernel.getModuleManager().install(mod);
  }

  @Test
  void ensureResolvingModuleWithoutDependencyFails() throws Exception {
    val ctx = resolve("test-plugin-2");
    val mod = ctx.getInstalledModule();
    assertEquals(
        "Module must be installed", mod.getLifecycle().getState(), ModuleLifecycle.State.Installed);

    kernel.getModuleManager().install(mod);
    assertThrows(
        UnsatisfiedDependencyException.class,
        () -> kernel.getModuleManager().resolve(mod),
        "must not allow module with unsatisified dependencies to be resolved");

    assertEquals(mod.getLifecycle().getState(), Lifecycle.State.Failed);
  }

  @Test
  void
      ensureInstallingModuleWithMissingDependencyThenInstallingDependencyThenResolvingOriginalWorks()
          throws Exception {
    val dependent = resolve("test-plugin-2").getInstalledModule();
    val dependency = resolve("test-plugin-1").getInstalledModule();

    kernel.getModuleManager().install(dependent);

    try {
      kernel.getModuleManager().resolve(dependent);
      fail("should have not been able to resolve module");
    } catch (UnsatisfiedDependencyException ex) {

    }

    kernel.getModuleManager().install(dependency);
    kernel.getModuleManager().resolve(dependency);
    kernel.getModuleManager().resolve(dependent);
  }
}
