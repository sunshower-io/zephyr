package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.ExecutorWorkerPool;
import io.sunshower.kernel.concurrency.KernelScheduler;
import io.sunshower.kernel.dependencies.DefaultDependencyGraph;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.kernel.module.ModuleInstallationGroup;
import io.sunshower.kernel.module.ModuleInstallationRequest;
import io.sunshower.kernel.module.ModuleLifecycle;
import io.sunshower.test.common.Tests;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultModuleManagerTest {

  Kernel kernel;
  ModuleManager manager;
  KernelScheduler<String> scheduler;
  private SunshowerKernelConfiguration cfg;

  @BeforeEach
  void setUp() {
    manager = new DefaultModuleManager(new DefaultDependencyGraph());
    scheduler = new KernelScheduler<>(new ExecutorWorkerPool(Executors.newFixedThreadPool(2)));

    val options = new KernelOptions();
    options.setHomeDirectory(Tests.createTemp("sunshower-kernel-tests"));
    SunshowerKernel.setKernelOptions(options);

    cfg =
        DaggerSunshowerKernelConfiguration.builder()
            .sunshowerKernelInjectionModule(
                new SunshowerKernelInjectionModule(options, ClassLoader.getSystemClassLoader()))
            .build();
    kernel = cfg.kernel();
    manager.initialize(kernel);
    kernel.start();
  }

  @AfterEach
  void tearDown() {
    kernel.stop();
  }

  @Test
  void ensureDownloadingAndInstallingModuleWorksCorrectly()
      throws MalformedURLException, ExecutionException, InterruptedException {
    //    val tempdir = Tests.createTemp();
    val plugin1 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-1", "war", "libs");
    val plugin2 =
        Tests.relativeToProjectBuild("kernel-tests:test-plugins:test-plugin-2", "war", "libs");

    val req1 = new ModuleInstallationRequest();
    req1.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req1.setLocation(plugin1.toURI().toURL());

    val req2 = new ModuleInstallationRequest();
    req2.setLifecycleActions(ModuleLifecycle.Actions.Install);
    req2.setLocation(plugin2.toURI().toURL());

    val grp = new ModuleInstallationGroup(req1, req2);

    val prepped = manager.prepare(grp);
    val result = scheduler.submit(prepped.getProcess()).get();
  }
}
