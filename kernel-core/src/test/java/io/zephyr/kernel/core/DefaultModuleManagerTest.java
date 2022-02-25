package io.zephyr.kernel.core;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.core.KernelLifecycle.State;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.DataflowAnomalyAnalysis",
    "PMD.JUnitAssertionsShouldIncludeMessage",
    "PMD.JUnitTestContainsTooManyAsserts"
})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DefaultModuleManagerTest extends ModuleManagerTestCase {

  @Test
  @SneakyThrows
  void ensureRetrievingModuleResourceWorks() {
    try {
      kernel.start();
      val grp = new ModuleInstallationGroup(req1);
      val prepped = manager.prepare(grp);
      prepped.commit().toCompletableFuture().get();
      start("plugin-1");
      val resource =
          manager.getModule(req1.getCoordinate()).getClassLoader().getResource("test.txt");
      assertNotNull(resource);
      assertEquals(
          manager.getModules(ModuleLifecycle.State.Active).size(), 1, "must be 1 started module");
    } finally {
      kernel.stop();
    }
  }

  @Test
  @SneakyThrows
  void ensureLoadingFlywayWorks() {
    try {
      kernel.start();

      val flywayPlugin =
          Tests.relativeToProjectBuild(
              "kernel-tests:test-plugins:test-plugin-flyway", "war", "libs");
      val request = new ModuleInstallationRequest();
      request.setLifecycleActions(ModuleLifecycle.Actions.Install);
      request.setLocation(flywayPlugin.toURI().toURL());

      val grp = new ModuleInstallationGroup(request);
      val prepped = manager.prepare(grp);
      scheduler.submit(prepped.getProcess()).get();
      start("test-plugin-flyway");
      val resource =
          kernel
              .getModuleManager()
              .getModule(request.getCoordinate())
              .getClassLoader()
              .getResource("flyway/V1_1__sample-test.sql");
      assertNotNull(resource);

    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureSavingKernelModuleWorks() throws Exception {

    //    final String className = "io.sunshower.yaml.state.YamlMementoProvider";
    val className = "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner";
    System.out.println("STATE " + kernel.getLifecycle().getState());
    if (kernel.getLifecycle().getState() == State.Running) {
      kernel.stop();
      return;
    }

    assertThrows(
        ClassNotFoundException.class,
        () -> {
          Class.forName(className, true, kernel.getClassLoader());
          fail("should not have been able to create a class");
        },
        "must not find class");

    val module =
        Tests.relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");

    val req1 = new ModuleInstallationRequest();
    req1.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req1.setLocation(module.toURI().toURL());

    val grp = new ModuleInstallationGroup(req1);

    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).get();

    assertThrows(
        ClassNotFoundException.class,
        () -> {
          Class.forName(className, true, kernel.getClassLoader());
        },
        "still must not be able to find class");

    tearDown();
    kernel.start();
    val cl = Class.forName(className, true, kernel.getClassLoader());
    assertNotNull(cl.getConstructor().newInstance(), "must be able to create");
  }

  @Test
  void ensureDownloadingAndInstallingModuleWorksCorrectly()
      throws ExecutionException, InterruptedException {
    val grp = new ModuleInstallationGroup(req1, req2);

    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).get();

    val resolved = manager.getModules(Lifecycle.State.Resolved);
    assertEquals(resolved.size(), 2);
  }

  @Test
  void ensureStartingPlugin1Works() throws ExecutionException, InterruptedException {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    assertEquals(
        manager.getModules(Lifecycle.State.Resolved).size(), 1, "must be 2 active plugins");
  }

  @Test
  void ensureIsntallingPluginsResultsInPluginsPlacedInResolvedState()
      throws ExecutionException, InterruptedException {

    val grp = new ModuleInstallationGroup(req2, req1);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    val active = manager.getModules(Lifecycle.State.Resolved);
    assertEquals(active.size(), 2, "must have 2 resolved plugins");
  }

  @Test
  void ensureStartingAndStoppingPluginsWorks() throws Exception {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    val activeModule = manager.getModules(Lifecycle.State.Resolved).get(0);
    start(activeModule.getCoordinate().getName());

    val req1action =
        new ModuleLifecycleChangeRequest(
            activeModule.getCoordinate(), ModuleLifecycle.Actions.Stop);
    val lgrp = new ModuleLifecycleChangeGroup(req1action);
    manager.prepare(lgrp).commit().toCompletableFuture().get();

    assertEquals(
        manager.getModules(ModuleLifecycle.State.Resolved).size(), 1, "must be one stopped module");
  }

  @Test
  void ensureStartingDependentPluginStartsBothPlugins() throws Exception {
    val grp = new ModuleInstallationGroup(req1, req2);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    start("plugin-2");
    assertEquals(
        manager.getModules(ModuleLifecycle.State.Active).size(), 2, "must be two started modules");
  }

  @Test
  void ensureStartingDependentPluginStartsDependency() throws Exception {

    val grp = new ModuleInstallationGroup(req1, req2);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    start("plugin-1");

    assertEquals(manager.getModules(Lifecycle.State.Active).size(), 1, "modules are equivalent");
  }

  @Test
  void ensureStartingAndStoppingInitiatorModuleWorks() throws Exception {
    kernel.stop();
    kernel.start();
    try {
      val grp = new ModuleInstallationGroup(req1, req2);
      val prepped = manager.prepare(grp);
      prepped.commit().toCompletableFuture().get();

      start("plugin-1");
      val p2 =
          manager.getModules(Lifecycle.State.Active).stream()
              .filter(t -> t.getCoordinate().getName().contains("plugin-1"))
              .findFirst()
              .get();

      val req1action =
          new ModuleLifecycleChangeRequest(p2.getCoordinate(), ModuleLifecycle.Actions.Stop);
      val lgrp = new ModuleLifecycleChangeGroup(req1action);
      manager.prepare(lgrp).commit().toCompletableFuture().get();
      for (int i = 0; i < 5; i++) {
        if (manager.getModules(Lifecycle.State.Resolved).size() == 2) {
          break;
        }
        Thread.sleep(1000);
      }

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
    val p2 =
        manager.getModules(Lifecycle.State.Resolved).stream()
            .filter(t -> t.getCoordinate().getName().contains("plugin-2"))
            .findFirst()
            .get();
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

  @Test
  void
  ensureInstallingSingleModuleResultsInModuleClasspathBeingConfiguredCorrectlyWithoutDependantClassesAppearing()
      throws Exception {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).get();

    val module = manager.getModules(Lifecycle.State.Resolved).get(0);
    assertThrows(
        ClassNotFoundException.class,
        () -> Class.forName("testproject2.Test", true, module.getClassLoader()),
        "shouldn't be able to find dependent class in this classloader");
  }

  @Test
  void ensureInstallingModuleWithDependentResultsInModuleClasspathBeingConfiguredCorrectly()
      throws Exception {

    val grp = new ModuleInstallationGroup(req2, req1);
    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).get();
    await().atMost(1, TimeUnit.SECONDS)
        .until(() -> manager
            .getModules(Lifecycle.State.Resolved
            ).stream().anyMatch(t -> "test-plugin-2".equals(t.getCoordinate().getName()))
        );

    val module =
        manager.getModules(Lifecycle.State.Resolved).stream()
            .filter(t -> "test-plugin-2".equals(t.getCoordinate().getName()))
            .findAny()
            .get();
    val result = Class.forName("testproject2.Test", true, module.getClassLoader());
    val t = result.getConstructor().newInstance();
    assertNotNull(t);
  }
}
