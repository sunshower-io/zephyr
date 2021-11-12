package io.zephyr.kernel.core;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.module.ModuleInstallationRequest;
import io.zephyr.kernel.module.ModuleLifecycle;
import io.zephyr.kernel.module.ModuleLifecycleChangeGroup;
import io.zephyr.kernel.module.ModuleLifecycleChangeRequest;
import java.io.File;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class ModuleManagerTestCase {

  protected Kernel kernel;
  protected ModuleManager manager;
  protected Scheduler<String> scheduler;
  protected SunshowerKernelConfiguration cfg;
  protected File plugin1;
  protected File plugin2;
  ModuleInstallationRequest req2;
  ModuleInstallationRequest req1;

  private File tempfile;

  @BeforeEach
  protected void setUp() throws Exception {

    val options = new KernelOptions();

    tempfile = configureFiles();
    options.setHomeDirectory(tempfile);

    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.factory()
            .create(options, ClassLoader.getSystemClassLoader());
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
    req2.setLifecycleActions(ModuleLifecycle.Actions.Activate);
    req2.setLocation(plugin2.toURI().toURL());
  }

  @AfterEach
  void tearDown() throws Exception {
    kernel.stop();
  }

  private File configureFiles() {
    return Tests.createTemp();
  }

  @SneakyThrows
  protected void start(String s) {
    request(s, ModuleLifecycle.Actions.Activate);
  }

  @SneakyThrows
  private void request(String pluginName, ModuleLifecycle.Actions action) {

    val plugin =
        manager.getModules().stream()
            .filter(t -> t.getCoordinate().getName().contains(pluginName))
            .findFirst()
            .get();

    val lifecycleRequest = new ModuleLifecycleChangeRequest(plugin.getCoordinate(), action);
    val grp = new ModuleLifecycleChangeGroup(lifecycleRequest);
    manager.prepare(grp).commit().toCompletableFuture().get();
  }
}
