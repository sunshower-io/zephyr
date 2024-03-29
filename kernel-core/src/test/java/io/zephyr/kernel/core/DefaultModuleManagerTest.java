package io.zephyr.kernel.core;

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
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import lombok.val;
import org.jboss.modules.ref.WeakReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
public class DefaultModuleManagerTest extends ModuleManagerTestCase {

  @BeforeEach
  void checkKernel() {
    assertEquals(State.Running, kernel.getLifecycle().getState());
  }

  @Test
  void ensureReadingResourceWorksFromDependentWorks() {
    val fst = moduleIn(":semver:test-plugin-3");
    install(fst);
    val result = find("test-plugin-3").getClassLoader().getResource("test.txt");
    assertNotNull(result);
  }

  @Test
  void ensureReadingVaadinResourceFromLibraryWorks() {

    val fst = moduleIn(":semver:test-plugin-3");
    install(fst);
    val result =
        find("test-plugin-3")
            .getClassLoader()
            .getResource("META-INF/resources/VAADIN/static/push/vaadinPush-min.js");
    assertNotNull(result);
  }

  @Test
  @SneakyThrows
  @Disabled("attempting to reproduce from local values")
  void ensureLoadingFromAireComponentsWorks() {
    install(
        new File(
            "/home/josiah/dev/src/github.com/aire-components/aire-ui/build/libs/aire-ui-1.0.16-SNAPSHOT-dist.war"));
    start("aire-ui");
    val result =
        find("aire-ui")
            .getClassLoader()
            .getResource("META-INF/resources/VAADIN/static/push/vaadinPush-min.js");
    assertNotNull(result);
    val stream = new InputStreamReader(result.openStream());
    int ch = 0;
    while ((ch = stream.read()) != -1) {
      System.out.print((char) ch);
    }
  }

  @Test
  void ensureReadingResourceWorks() {
    val fst = moduleIn(":semver:test-plugin-1");
    val snd = moduleIn(":semver:test-plugin-2-0");
    install(fst, snd);
    start("test-plugin-1");
    val result = find("test-plugin-1").getClassLoader().getResource("test.txt");
    assertNotNull(result);
  }

  @Test
  void ensureReadingClassWorks() {
    val fst = moduleIn(":semver:test-plugin-1");
    val snd = moduleIn(":semver:test-plugin-2-0");
    install(fst, snd);
    start("test-plugin-1");
    val result = find("test-plugin-1").getClassLoader().getResource("plugin1/Service.class");
    assertNotNull(result);
  }

  @Test
  void ensureSemverWorksForSingleVersion() {

    val fst = moduleIn(":semver:test-plugin-1");
    val snd = moduleIn(":semver:test-plugin-2-0");
    install(fst, snd);
    start("test-plugin-2");
    var result = invokeServiceOn("test-plugin-1", "plugin1.Service", "getHello");
    assertEquals("Hello from 1.0", result);
    result = invokeClassOn("test-plugin-2", "plugin1.Plugin1Service", "getHello");
    assertEquals("Hello from 1.0", result);
  }

  @Test
  @SneakyThrows
  void ensureStoppingModuleResultsInClassesBeingUnloaded() {

    val fst = moduleIn(":semver:test-plugin-1");
    install(fst);
    start("test-plugin-1");

    val queue = new ReferenceQueue<>();
    var type =
        new WeakReference<>(findClass("test-plugin-1", "plugin1.Plugin1Service"), this, queue);
    assertNotNull(type.get());
    Modules.close(find("test-plugin-1"), kernel);
    Modules.start(find("test-plugin-1"), kernel);

    type = new WeakReference<>(findClass("test-plugin-1", "plugin1.Plugin1Service"), this, queue);
    assertNotNull(type.get());
  }

  @Test
  void ensureSemverWorksForMultipleVersions() {
    val fst = moduleIn(":semver:test-plugin-1");
    val fst1 = moduleIn(":semver:test-plugin-1-1");
    val snd = moduleIn(":semver:test-plugin-2-0");
    install(fst, snd, fst1);
    start("test-plugin-2");

    val result = invokeClassOn("test-plugin-2", "plugin1.Plugin1Service", "getHello");
    assertEquals("Hello from 1.1", result);
  }

  @Test
  @SneakyThrows
  void ensureRetrievingModuleResourceWorks() {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    prepped.commit().toCompletableFuture().get();
    start("plugin-1");
    val resource = manager.getModule(req1.getCoordinate()).getClassLoader().getResource("test.txt");
    assertNotNull(resource);
    assertEquals(
        manager.getModules(ModuleLifecycle.State.Active).size(), 1, "must be 1 started module");
  }

  @Test
  @SneakyThrows
  void ensureLoadingFlywayWorks() {
    val flywayPlugin =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-flyway", "war", "libs");
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
  }

  @Test
  void ensureSavingKernelModuleWorks() throws Exception {

    //    final String className = "io.sunshower.yaml.state.YamlMementoProvider";
    val className = "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner";

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
    val cl = Class.forName(className, false, kernel.getClassLoader());
    //    assertNotNull(cl.getConstructor().newInstance(), "must be able to create");
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
    val module = find("test-plugin-2");
    val result = Class.forName("testproject2.Test", true, module.getClassLoader());
    val t = result.getConstructor().newInstance();
    assertNotNull(t);
  }
}
