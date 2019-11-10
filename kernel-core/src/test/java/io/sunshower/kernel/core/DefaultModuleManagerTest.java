package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.concurrency.ExecutorWorkerPool;
import io.sunshower.kernel.concurrency.KernelScheduler;
import io.sunshower.kernel.concurrency.Scheduler;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import io.sunshower.kernel.module.ModuleInstallationStatusGroup;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.test.common.Tests;
import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
class DefaultModuleManagerTest {

  Kernel kernel;
  ModuleManager manager;
  Scheduler<String> scheduler;
  SunshowerKernelConfiguration cfg;

  File plugin1;
  File plugin2;
  ModuleInstallationRequest req2;
  ModuleInstallationRequest req1;

  @BeforeEach
  void setUp() throws Exception {

    val options = new KernelOptions();
    val tempfile = configureFiles();
    options.setHomeDirectory(tempfile);

    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();
    kernel = cfg.kernel();
    manager = kernel.getModuleManager();
    manager.initialize(kernel);
    scheduler = kernel.getScheduler();
    kernel.start();

    plugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    plugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");

    req1 = new ModuleInstallationRequest();
    req1.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req1.setLocation(plugin1.toURI().toURL());

    req2 = new ModuleInstallationRequest();
    req2.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req2.setLocation(plugin2.toURI().toURL());

  }

  @AfterEach
  void tearDown() {
    kernel.stop();
  }

  @Test
  void ensureSavingKernelModuleWorks() throws Exception {

    assertThrows(
        ClassNotFoundException.class,
        () -> {
          Class.forName(
              "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner",
              true,
              kernel.getClassLoader());
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
          Class.forName(
              "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner",
              true,
              kernel.getClassLoader());
        },
        "still must not be able to find class");

    kernel.stop();
    kernel.start();
    val cl =
        Class.forName(
            "io.sunshower.kernel.ext.scanner.YamlPluginDescriptorScanner",
            true,
            kernel.getClassLoader());
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
  void ensureInstallingSingleModuleResultsInModuleClasspathBeingConfiguredCorrectly()
      throws Exception {
    val grp = new ModuleInstallationGroup(req1);
    val prepped = manager.prepare(grp);
    scheduler.submit(prepped.getProcess()).get();

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

    val module =
        manager.getModules(Lifecycle.State.Resolved).stream()
            .filter(t -> t.getCoordinate().getName().equals("test-plugin-2"))
            .findAny()
            .get();
    val result = Class.forName("testproject2.Test", true, module.getClassLoader());
    val t = result.getConstructor().newInstance();
    assertNotNull(t);
  }

  private File configureFiles() {
    val tempfile = Tests.createTemp();
    return tempfile;
  }
}
