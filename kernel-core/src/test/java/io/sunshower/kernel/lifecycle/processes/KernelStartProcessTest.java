package io.sunshower.kernel.lifecycle.processes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import io.sunshower.kernel.concurrency.*;
import io.sunshower.kernel.core.ModuleManager;
import io.sunshower.kernel.core.SunshowerKernel;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class KernelStartProcessTest {

  private Context context;
  private SunshowerKernel kernel;
  private KernelOptions kernelOptions;
  private Scheduler<String> scheduler;

  @BeforeEach
  void setUp() {
    kernelOptions = new KernelOptions();
    kernelOptions.setHomeDirectory(Tests.createTemp());
    SunshowerKernel.setKernelOptions(kernelOptions);
    kernel = spy(new SunshowerKernel(mock(ModuleManager.class), mock(ExecutorService.class)));

    context = ReductionScope.newContext();
    context.set("SunshowerKernel", kernel);
    scheduler = new KernelScheduler<>(new ExecutorWorkerPool(Executors.newFixedThreadPool(2)));
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
            .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
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
            .register("kernel:lifecycle:module:list", new KernelModuleListReadPhase())
            .dependsOn("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
            .create();
    val tracker = scheduler.submit(process);
    tracker.get();
    val scope = tracker.getCurrentScope();
    assertNotNull(
        scope.resolveValue(KernelModuleListReadPhase.INSTALLED_MODULE_LIST),
        "module list must be populated");
  }

  @Test
  void ensureKernelClassloaderCreationWorks() throws ExecutionException, InterruptedException {
    val process =
        Tasks.newProcess("kernel:start:filesystem")
            .withContext(context)
            .register("kernel:lifecycle:module:list", new KernelModuleListReadPhase())
            .register("kernel:lifecycle:filesystem:create", new KernelFilesystemCreatePhase())
            .register("kernel:lifecycle:classloader", new KernelClassLoaderCreationPhase())
            .task("kernel:lifecycle:module:list")
            .dependsOn("kernel:lifecycle:filesystem:create")
            .task("kernel:lifecycle:classloader")
            .dependsOn("kernel:lifecycle:module:list")
            .create();
    val tracker = scheduler.submit(process);
    tracker.get();
    val scope = tracker.getCurrentScope();
    assertNotNull(
        scope.resolveValue(KernelModuleListReadPhase.INSTALLED_MODULE_LIST),
        "module list must be populated");
  }
}
