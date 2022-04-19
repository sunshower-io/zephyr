package io.zephyr.kernel.core;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.util.concurrent.TimeUnit;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

@DisabledIfEnvironmentVariable(
    named = "BUILD_ENVIRONMENT",
    matches = "github",
    disabledReason = "RMI is flaky")
@SuppressWarnings({
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
public class DefaultModuleManagerExtTest extends ModuleManagerTestCase {

  @Test
  void ensureStartingAndStoppingInitiatorModuleWorks() throws Exception {
    kernel.start();
    try {
      val grp = new ModuleInstallationGroup(req1, req2);
      val prepped = manager.prepare(grp);
      prepped.commit().toCompletableFuture().get();

      start("plugin-1");
      start("plugin-2");
      val p2 = find("plugin-1");

      val req1action =
          new ModuleLifecycleChangeRequest(p2.getCoordinate(), ModuleLifecycle.Actions.Stop);
      val lgrp = new ModuleLifecycleChangeGroup(req1action);
      manager.prepare(lgrp).commit().toCompletableFuture().get();
      await()
          .atMost(10, TimeUnit.SECONDS)
          .until(() -> manager.getModules(Lifecycle.State.Resolved).size() == 2);
      assertEquals(manager.getModules(Lifecycle.State.Active).size(), 0);
      assertEquals(manager.getModules(Lifecycle.State.Resolved).size(), 2);
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureStartingAndStoppingMultipleModulesWorks() throws Exception {
    val grp = new ModuleInstallationGroup(req1, req2);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    val p2 = find("plugin-2");
    start("plugin-2");

    val req1action =
        new ModuleLifecycleChangeRequest(p2.getCoordinate(), ModuleLifecycle.Actions.Stop);
    val lgrp = new ModuleLifecycleChangeGroup(req1action);
    manager.prepare(lgrp).commit().toCompletableFuture().get();

    assertEquals(manager.getModules(Lifecycle.State.Active).size(), 1);
    assertEquals(manager.getModules(Lifecycle.State.Resolved).size(), 1);
  }

  @Test
  void ensureInstallingSingleModuleResultsInModuleClasspathBeingConfiguredCorrectly()
      throws Exception {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).toCompletableFuture().get();
    await()
        .atMost(1, TimeUnit.SECONDS)
        .until(() -> !manager.getModules(Lifecycle.State.Resolved).isEmpty());
    val module = manager.getModules(Lifecycle.State.Resolved).get(0);
    val result = Class.forName("plugin1.Test", true, module.getClassLoader());
    val t = result.getConstructor().newInstance();
    assertNotNull(t);
  }
}
