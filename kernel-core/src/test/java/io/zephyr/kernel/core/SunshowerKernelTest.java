package io.zephyr.kernel.core;

import static io.sunshower.test.common.Tests.relativeToProjectBuild;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zephyr.kernel.KernelTestCase;
import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.misc.SuppressFBWarnings;
import io.zephyr.kernel.module.ModuleInstallationGroup;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@Log
@SuppressFBWarnings
@SuppressWarnings({
  "PMD.AvoidInstantiatingObjectsInLoops",
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
@DisabledOnOs(OS.WINDOWS)
public class SunshowerKernelTest extends KernelTestCase {

  @Test
  void ensureKernelIsCreated() {
    kernel = cfg.kernel();
    assertNotNull(kernel, "kernel must not be null");
  }

  @Test
  void ensureInjectionOfPluginManagerWorks() {
    kernel = cfg.kernel();
    assertNotNull(kernel.getModuleManager(), "plugin manager must be injected");
  }

  @Test
  void ensureStartingKernelProducesFileSystem() throws IOException {
    assertNull(kernel.getFileSystem(), "kernel filesystem must initially be null");
    kernel.start();
    try {
      assertNotNull(kernel.getFileSystem(), "kernel filesystem must now be set");
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureStartingKernelProducesClassLoader() throws IOException {
    assertNull(kernel.getClassLoader(), "kernel filesystem must initially be null");
    kernel.start();
    try {
      assertNotNull(kernel.getClassLoader(), "kernel filesystem must now be set");
    } finally {
      kernel.stop();
    }
  }

  @RepeatedTest(10)
  void ensureKernelLoadingIsIdempotent() throws InterruptedException {
    try {
      kernel.start();
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureStartingSpringBootPluginFileWorks() throws InterruptedException {

    try {
      installYamlModule();

      springPlugin = relativeToProjectBuild("plugins:spring:spring-web-plugin", "war", "libs");
      install(springPlugin);
      start("spring-web-plugin");
      stop("spring-web-plugin");
      remove("spring-web-plugin");
    } finally {
      kernel.stop();
    }
  }

  private void installYamlModule() {
    kernel.start();
    yamlModule = relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
    install(yamlModule);
    kernel.stop();
    kernel.start();
  }

  @Test
  void ensureTransitiveSpringWebPluginWorks() throws InterruptedException {
    try {
      installYamlModule();
      springPlugin = relativeToProjectBuild("plugins:spring:spring-web-plugin", "war", "libs");
      install(springPlugin);

      springPlugin =
          relativeToProjectBuild("kernel-tests:test-plugins:test-spring-web-plugin", "war", "libs");
      install(springPlugin);
      start("test-spring-web-plugin");

      stop("test-spring-web-plugin");
      remove("spring-web-plugin");
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureRemovingPluginWorks() {
    try {
      installYamlModule();
      springPlugin =
          relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");
      install(springPlugin);
      assertEquals(kernel.getModuleManager().getModules().size(), 1, "must have one plugin");

      remove("spring-plugin");

      assertTrue(kernel.getModuleManager().getModules().isEmpty(), "must have no plugins");
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureInstallingAndStartingInvalidPluginFails() {

    installYamlModule();

    springPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");

    install(springPlugin);

    springPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring-error", "war", "libs");
    install(springPlugin);
    start("spring-plugin-error");
    val failed = kernel.getModuleManager().getModules(Lifecycle.State.Failed);
    assertEquals(failed.size(), 1, "must have one failed plugin");
  }

  @Test
  void ensureInstallingDependentPluginWorks() throws InterruptedException {

    kernel.start();
    yamlModule = relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
    springPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");
    install(yamlModule);
    kernel.stop();
    kernel.start();
    try {
      install(springPlugin);

      start("spring-plugin");
      //    stop("spring-plugin");

      var depPlugin =
          relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring-dep", "war", "libs");
      install(depPlugin);
      start("spring-plugin-dep");

      stop("spring-plugin");
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureRetrievingClassloaderResourceOnInstalledPluginWorksForWARFormatWorks() {
    kernel.start();

    yamlModule = relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
    springPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");
    install(yamlModule);
    kernel.stop();
    kernel.start();
    install(springPlugin);

    try {
      val module = resolveModule("spring-plugin");
      val resource = module.getClassLoader().getResource("public/frap.txt");
      assertNotNull(resource);
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureRetrievingClassloaderResourceOnInstalledPluginWorksForJARFormatWorks() {
    kernel.start();

    yamlModule = relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
    springPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring-jar", "jar", "libs");
    install(yamlModule);
    kernel.stop();
    kernel.start();
    install(springPlugin);
    try {

      val module = resolveModule("spring-plugin-jar");
      val resource = module.getClassLoader().getResource("public/frap.txt");
      assertNotNull(resource);
    } finally {
      kernel.stop();
    }
  }

  @Test
  void ensureStartingSpringBootPluginWorks() {

    try {
      kernel.start();
      yamlModule = relativeToProjectBuild("kernel-modules:sunshower-yaml-reader", "war", "libs");
      springPlugin =
          relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring", "war", "libs");
      install(yamlModule);
      kernel.stop();
      kernel.start();
      install(springPlugin);

      start("spring-plugin");
      stop("spring-plugin");
    } finally {
      kernel.stop();
    }
  }

  private void stop(String s) {

    request(s, ModuleLifecycle.Actions.Stop);
  }

  private void remove(String name) {
    request(name, ModuleLifecycle.Actions.Delete);
  }

  @SneakyThrows
  private void install(File... files) {
    val group = new ModuleInstallationGroup();
    for (val file : files) {
      val request = new ModuleInstallationRequest();
      request.setLocation(file.toURI().toURL());
      request.setLifecycleActions(ModuleLifecycle.Actions.Activate);
      group.add(request);
    }
    kernel.getModuleManager().prepare(group).commit().toCompletableFuture().get();
  }

  @SneakyThrows
  private void start(String s) {
    request(s, ModuleLifecycle.Actions.Activate);
  }

  @SneakyThrows
  private void request(String pluginName, ModuleLifecycle.Actions action) {

    val plugin =
        kernel.getModuleManager().getModules().stream()
            .filter(t -> t.getCoordinate().getName().equals(pluginName))
            .findFirst()
            .get();

    val lifecycleRequest = new ModuleLifecycleChangeRequest(plugin.getCoordinate(), action);
    val grp = new ModuleLifecycleChangeGroup(lifecycleRequest);
    kernel.getModuleManager().prepare(grp).commit().toCompletableFuture().get();
  }

  private Module resolveModule(String name) {
    return kernel.getModuleManager().getModules().stream()
        .filter(t -> t.getCoordinate().getName().equals(name))
        .findAny()
        .get();
  }
}
