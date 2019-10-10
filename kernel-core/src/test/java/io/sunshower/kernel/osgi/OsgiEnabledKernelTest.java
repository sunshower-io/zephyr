package io.sunshower.kernel.osgi;

import static io.sunshower.test.common.Tests.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Kernel;
import io.sunshower.kernel.KernelExtensionLoadTask;
import io.sunshower.kernel.KernelModuleManager;
import io.sunshower.kernel.PluginManager;
import io.sunshower.kernel.launch.KernelOptions;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OsgiEnabledKernelTest {

  private File storage;
  private Kernel kernel;
  private KernelOptions options;
  private PluginManager pluginManager;
  private KernelModuleManager kernelManager;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    storage = createTemp("sunshower-temp");
    options.overrideStorage(storage.getAbsolutePath());
    kernel = new OsgiEnabledKernel(options);
    pluginManager = kernel.getPluginManager();
    kernelManager = kernel.getModuleManager();
  }

  @Test
  void ensureDirectoriesAreCreated() {
    assertTrue(options.getWorkspaceDirectory().toFile().exists());
    assertTrue(options.getPluginDirectory().toFile().exists());
    assertTrue(options.getPluginTempDirectory().toFile().exists());
    assertTrue(options.getKernelModuleDirectory().toFile().exists());
  }

  @Test
  @SneakyThrows
  void ensureTaskLifecycleIsCorrect() {
    val pf = loadTestPlugin("test-plugin-1", "jar");

    val task = pluginManager.loadExtensionFile(pf);
    assertEquals(task.getState(), KernelExtensionLoadTask.State.Unstarted);
    assertNull(task.getError());
    assertFalse(task.isComplete());
    assertEquals(pluginManager.getInflight().size(), 1);
    task.start();
    val descriptor = task.getFuture().get();

    assertNotNull(descriptor);
    assertNull(task.getError());
    assertTrue(task.isComplete());
    assertTrue(pluginManager.getInflight().isEmpty());
    assertEquals(pluginManager.getLoaded().size(), 1);
  }

  @Test
  void ensurePluginIsInstalledToCorrectPlace() throws ExecutionException, InterruptedException {
    val pf = loadTestPlugin("test-plugin-1", "jar");
    val task = pluginManager.loadExtensionFile(pf);
    task.start();
    task.getFuture().get();
    val deployed =
        relativeToCurrentProjectBuild(
            "jar", "temp", "sunshower-temp", ".sunshower", "workspace", "plugins");
  }

  @Test
  @SneakyThrows
  void ensureModuleIsInstalledToCorrectLocation() {
    val km = loadTestModule("sunshower-yaml-reader", "jar");
    val task = kernelManager.loadExtensionFile(km);
    task.start();
    task.getFuture().get();
  }

  @SneakyThrows
  private URL loadTestModule(String module, String ext) {
    return projectOutput(format("kernel-modules:%s", module), ext).toURI().toURL();
  }

  @SneakyThrows
  private URL loadTestPlugin(String name, String ext) {
    return projectOutput(format("kernel-tests:test-plugins:%s", name), ext).toURI().toURL();
  }
}
