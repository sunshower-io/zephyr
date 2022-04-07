package io.zephyr.kernel.lifecycle.processes;

import static org.mockito.Mockito.*;

import io.sunshower.checks.SuppressFBWarnings;
import io.sunshower.gyre.Scope;
import io.sunshower.test.common.Tests;
import io.zephyr.api.ServiceRegistry;
import io.zephyr.kernel.concurrency.ExecutorWorkerPool;
import io.zephyr.kernel.concurrency.KernelScheduler;
import io.zephyr.kernel.concurrency.Scheduler;
import io.zephyr.kernel.concurrency.Tasks;
import io.zephyr.kernel.core.ModuleClasspathManager;
import io.zephyr.kernel.core.ModuleManager;
import io.zephyr.kernel.core.SunshowerKernel;
import io.zephyr.kernel.core.lifecycle.KernelClassLoaderCreationPhase;
import io.zephyr.kernel.core.lifecycle.KernelFilesystemCreatePhase;
import io.zephyr.kernel.core.lifecycle.KernelModuleListReadPhase;
import io.zephyr.kernel.launch.KernelOptions;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@SuppressFBWarnings
@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.DataflowAnomalyAnalysis",
  "PMD.JUnitAssertionsShouldIncludeMessage",
  "PMD.JUnitTestContainsTooManyAsserts"
})
class KernelStartProcessTest {

  private Scope context;
  private SunshowerKernel kernel;
  private KernelOptions kernelOptions;
  private Scheduler<String> scheduler;

  @BeforeEach
  void setUp() {
    kernelOptions = new KernelOptions();
    kernelOptions.setHomeDirectory(Tests.createTemp());
    SunshowerKernel.setKernelOptions(kernelOptions);

    context = Scope.root();
    scheduler =
        new KernelScheduler<>(
            new ExecutorWorkerPool(
                Executors.newFixedThreadPool(2), Executors.newFixedThreadPool(2)));
    kernel =
        spy(new SunshowerKernel(mock(ModuleManager.class), mock(ServiceRegistry.class), scheduler));
    kernel.setModuleClasspathManager(mock(ModuleClasspathManager.class));
    context.set("SunshowerKernel", kernel);
  }

  @AfterEach
  void tearDown() throws IOException {
    FileSystems.getFileSystem(URI.create("droplet://kernel")).close();
  }

  @RepeatedTest(2)
  void ensureFilesystemProcessCreatesFilesystem() throws ExecutionException, InterruptedException {
    val process =
        Tasks.newProcess("kernel:start:filesystem")
            .withContext(context)
            .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
            .create();

    val tracker = scheduler.submit(process);
    tracker.get();
    verify(kernel, times(1)).setFileSystem(any());
  }

  @Test
  void ensureFilesystemStartProcessWithKernelModuleListReadPhaseWorks()
      throws ExecutionException, InterruptedException {
    val process =
        Tasks.newProcess("kernel:start:filesystem")
            .withContext(context)
            .register(new KernelModuleListReadPhase("kernel:lifecycle:module:list"))
            .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
            .task("kernel:lifecycle:module:list")
            .dependsOn("kernel:lifecycle:filesystem:create")
            .create();
    val tracker = scheduler.submit(process);
    tracker.get();
  }

  @Test
  void ensureKernelClassloaderCreationWorks() throws ExecutionException, InterruptedException {
    val process =
        Tasks.newProcess("kernel:start:filesystem")
            .withContext(context)
            .register(new KernelModuleListReadPhase("kernel:lifecycle:module:list"))
            .register(new KernelFilesystemCreatePhase("kernel:lifecycle:filesystem:create"))
            .register(new KernelClassLoaderCreationPhase("kernel:lifecycle:classloader"))
            .task("kernel:lifecycle:module:list")
            .dependsOn("kernel:lifecycle:filesystem:create")
            .task("kernel:lifecycle:classloader")
            .dependsOn("kernel:lifecycle:module:list")
            .create();
    val tracker = scheduler.submit(process);
    tracker.get();
  }
}
