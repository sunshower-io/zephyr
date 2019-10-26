package io.sunshower.kernel.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.ModuleLifecycle;
import io.sunshower.kernel.dependencies.UnsatisfiedDependencyException;
import io.sunshower.module.phases.AbstractModulePhaseTestCase;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.EmptyCatchBlock",
  "PMD.JUnitUseExpected",
  "PMD.AvoidDuplicateLiterals",
  "PMD.UseProperClassLoader",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage",
})
class DefaultModuleManagerTest extends AbstractModulePhaseTestCase {
  @Test
  void ensureInstallingAndResolvingModuleWithNoDependenciesWorks() throws Exception {
    val ctx = resolve("test-plugin-1");
    val mod = ctx.getInstalledModule();
    try {

      assertEquals(
          "Module must be installed",
          mod.getLifecycle().getState(),
          ModuleLifecycle.State.Installed);

      kernel.getModuleManager().install(mod);
      kernel.getModuleManager().resolve(mod);

      assertEquals(
          "Module must be resolved", mod.getLifecycle().getState(), ModuleLifecycle.State.Resolved);
    } finally {
      mod.getFileSystem().close();
    }
  }

  @Test
  @DisplayName("modules must be installable but not resolvable")
  void ensureInstallingModuleWithoutDependencySucceeds() throws Exception {
    val ctx = resolve("test-plugin-2");
    val mod = ctx.getInstalledModule();
    try {
      assertEquals(
          "Module must be installed",
          mod.getLifecycle().getState(),
          ModuleLifecycle.State.Installed);

      kernel.getModuleManager().install(mod);
    } finally {
      mod.getFileSystem().close();
    }
  }

  @Test
  void ensureResolvingModuleWithoutDependencyFails() throws Exception {
    val ctx = resolve("test-plugin-2");
    val mod = ctx.getInstalledModule();
    try {
      assertEquals(
          "Module must be installed",
          mod.getLifecycle().getState(),
          ModuleLifecycle.State.Installed);

      kernel.getModuleManager().install(mod);
      assertThrows(
          UnsatisfiedDependencyException.class,
          () -> kernel.getModuleManager().resolve(mod),
          "must not allow module with unsatisified dependencies to be resolved");

      assertEquals(mod.getLifecycle().getState(), Lifecycle.State.Failed);
    } finally {
      mod.getFileSystem().close();
    }
  }

  @Test
  void
      ensureInstallingModuleWithMissingDependencyThenInstallingDependencyThenResolvingOriginalWorks()
          throws Exception {
    val dependent = resolve("test-plugin-2").getInstalledModule();
    val dependency = resolve("test-plugin-1").getInstalledModule();

    try {
      kernel.getModuleManager().install(dependent);

      try {
        kernel.getModuleManager().resolve(dependent);
        fail("should have not been able to resolve module");
      } catch (UnsatisfiedDependencyException ex) {

      }

      kernel.getModuleManager().install(dependency);
      kernel.getModuleManager().resolve(dependency);
      kernel.getModuleManager().resolve(dependent);
    } finally {
      dependency.getFileSystem().close();
      dependent.getFileSystem().close();
    }
  }

  @Test
  void ensureActionTreeIsCorrectForIndependentModule() throws Exception {
    val dependent = resolve("test-plugin-1").getInstalledModule();
    try {
      kernel.getModuleManager().install(dependent);
      kernel.getModuleManager().resolve(dependent);

      val action = kernel.getModuleManager().prepareFor(Lifecycle.State.Starting, dependent);
      assertEquals("Action tree must have at least one element", action.getActionTree().size(), 1);
      assertEquals("Action tree height must be one", action.getActionTree().height(), 1);
    } finally {
      dependent.getFileSystem().close();
    }
  }

  @Test
  void ensureActionTreeIsCorrectForDependentModule() throws Exception {
    val first = resolve("test-plugin-1").getInstalledModule();

    val dependent = resolve("test-plugin-2").getInstalledModule();
    try {
      kernel.getModuleManager().install(first);
      kernel.getModuleManager().install(dependent);
      kernel.getModuleManager().resolve(first);
      kernel.getModuleManager().resolve(dependent);

      val action = kernel.getModuleManager().prepareFor(Lifecycle.State.Starting, dependent);
      assertEquals("Action tree must have at least one element", action.getActionTree().size(), 2);
      assertEquals("Action tree height must be one", action.getActionTree().height(), 2);
    } finally {
      first.getFileSystem().close();
      dependent.getFileSystem().close();
    }
  }
}
