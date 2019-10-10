package io.sunshower.kernel.osgi;

import io.sunshower.kernel.Kernel;
import io.sunshower.kernel.KernelExtensionLoadTask;
import io.sunshower.kernel.PluginManager;
import io.sunshower.kernel.launch.KernelOptions;
import io.sunshower.test.common.Tests;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static io.sunshower.test.common.Tests.createTemp;
import static io.sunshower.test.common.Tests.projectOutput;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

class OsgiEnabledKernelTest {

  private File storage;
  private Kernel kernel;
  private KernelOptions options;
  private PluginManager pluginManager;

  @BeforeEach
  void setUp() {
    options = new KernelOptions();
    storage = createTemp("sunshower-temp");
    options.overrideStorage(storage.getAbsolutePath());
    kernel = new OsgiEnabledKernel(options);
    pluginManager = kernel.getPluginManager();
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

    val task = pluginManager.load(pf);
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

  @SneakyThrows
  private URL loadTestPlugin(String name, String ext) {
    return projectOutput(format("kernel-tests:test-plugins:%s", name), ext).toURI().toURL();
  }
}
