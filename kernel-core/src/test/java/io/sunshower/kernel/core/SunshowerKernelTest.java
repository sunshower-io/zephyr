package io.sunshower.kernel.core;

import static io.sunshower.test.common.Tests.relativeToProjectBuild;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.misc.SuppressFBWarnings;
import io.sunshower.kernel.module.*;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

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
public class SunshowerKernelTest {

  private Kernel kernel;
  private File yamlModule;
  private File springPlugin;
  private SunshowerKernelConfiguration cfg;

  @BeforeEach
  void setUp() {
    val options = new KernelOptions();
    options.setHomeDirectory(Tests.createTemp());
    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();
    kernel = cfg.kernel();
  }

  @AfterEach
  void tearDown() throws IOException {
    try {
      FileSystems.getFileSystem(URI.create("droplet://kernel")).close();
    } catch (Exception ex) {
      log.info(ex.getMessage());
    }
  }

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
    assertNotNull(kernel.getFileSystem(), "kernel filesystem must now be set");
  }

  @Test
  void ensureStartingKernelProducesClassLoader() throws IOException {
    assertNull(kernel.getClassLoader(), "kernel filesystem must initially be null");
    kernel.start();
    assertNotNull(kernel.getClassLoader(), "kernel filesystem must now be set");
  }

  @Test
  @RepeatedTest(10)
  void ensureKernelLoadingIsIdempotent() throws InterruptedException {
    kernel.start();
    kernel.stop();
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
    install(springPlugin);

    start("spring-plugin");
    //    stop("spring-plugin");

    var depPlugin =
        relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-spring-dep", "war", "libs");
    install(depPlugin);
    start("spring-plugin-dep");

    stop("spring-plugin");
  }

  @Test
  void ensureStartingSpringBootPluginWorks() {

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
  }

  private void stop(String s) {

    request(s, ModuleLifecycle.Actions.Stop);
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
        kernel
            .getModuleManager()
            .getModules()
            .stream()
            .filter(t -> t.getCoordinate().getName().equals(pluginName))
            .findFirst()
            .get();

    val lifecycleRequest = new ModuleLifecycleChangeRequest(plugin.getCoordinate(), action);
    val grp = new ModuleLifecycleChangeGroup(lifecycleRequest);
    kernel.getModuleManager().prepare(grp).commit().toCompletableFuture().get();
  }
}
