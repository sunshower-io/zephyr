package io.zephyr.api;

import static io.zephyr.api.ModuleEvents.INSTALLED;
import static io.zephyr.api.ModuleEvents.INSTALL_FAILED;
import static io.zephyr.api.ModuleEvents.STARTED;
import static io.zephyr.api.ModuleEvents.STARTING;
import static io.zephyr.api.ModuleEvents.START_FAILED;
import static io.zephyr.kernel.modules.shell.ShellTestCase.TestPlugins.TEST_PLUGIN_1;
import static io.zephyr.kernel.modules.shell.ShellTestCase.TestPlugins.TEST_PLUGIN_2;
import static io.zephyr.kernel.modules.shell.ShellTestCase.TestPlugins.TEST_PLUGIN_SPRING;
import static io.zephyr.kernel.modules.shell.ShellTestCase.TestPlugins.TEST_PLUGIN_SPRING_DEP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.core.KernelLifecycle.State;
import io.zephyr.kernel.launch.KernelOptions;
import io.zephyr.kernel.modules.shell.ShellTestCase;
import java.io.File;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ModuleListenerTestCase extends ShellTestCase {

  static int count = 0;

  static {
    Logger logger;
    for (logger = Logger.getLogger(ModuleListenerTestCase.class.getName());
        logger.getParent() != null;
        logger = logger.getParent()) {}

    logger.setLevel(Level.SEVERE);
  }

  @Mock protected ModuleListener listener;

  public ModuleListenerTestCase() {
    super(false);
  }

  @Override
  @BeforeEach
  @SneakyThrows
  protected void setUp() {
    val hd = Tests.createTemp(UUID.randomUUID().toString());
    if (homeDirectory != null) {
      assertNotEquals(hd, homeDirectory);
    }
    homeDirectory = hd;
    start();
    assertSame(kernel.getLifecycle().getState(), State.Running);

    assertEquals(KernelOptions.getKernelRootDirectory(), homeDirectory);
    assertEquals(0, moduleCount());
    assertEquals(0, kernelModuleCount());
    count++;
  }

  @Override
  @AfterEach
  protected void tearDown() {
    stop();
  }

  @Test
  void ensureInstallingYamlProducesCorrectNumberOfKernelModules() {
    install(StandardModules.YAML);
    restartKernel();
    assertEquals(1, kernel.getKernelModules().size());
  }

  @Test
  void ensurePluginInstallingDispatchesEvent() {
    kernel.addEventListener(listener, ModuleEvents.INSTALLING);
    install(StandardModules.YAML);
    verify(listener).onEvent(eq(ModuleEvents.INSTALLING), any());
  }

  @Test
  void ensureAttemptingToInstallInvalidPluginDispatchesFailedEvent() {

    kernel.addEventListener(listener, INSTALLED, INSTALL_FAILED);
    install(new FileInstallable(new File("nothere")));
    verify(listener, timeout(1000).times(1)).onEvent(eq(INSTALL_FAILED), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(INSTALLED), any());
    kernel.removeEventListener(listener);
  }

  @Test
  void ensureSuccessfullyInstallingKernelModuleProducesSucceededEvent() {
    kernel.addEventListener(listener, INSTALLED, INSTALL_FAILED);
    install(StandardModules.YAML);
    verify(listener, timeout(1000).times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
    kernel.removeEventListener(listener);
  }

  @Test
  void ensureSuccessfullyInstallingPluginProducesSucceededEvent() {
    install(StandardModules.YAML);
    restart();
    kernel.addEventListener(
        listener, ModuleEvents.INSTALLED, ModuleEvents.INSTALL_FAILED, ModuleEvents.INSTALLING);
    installAndWaitForModuleCount(1, TEST_PLUGIN_1);
    verify(listener, timeout(1000).times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
    verify(listener, timeout(1000).times(1)).onEvent(eq(ModuleEvents.INSTALLING), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
  }

  @Test
  void ensureSuccessfullyInstallingMultiplePluginsProducesSucceededEvent() {
    install(StandardModules.YAML);
    restart();
    kernel.addEventListener(
        listener, ModuleEvents.INSTALLED, ModuleEvents.INSTALL_FAILED, ModuleEvents.INSTALLING);

    installAndWaitForModuleCount(2, TEST_PLUGIN_1, TEST_PLUGIN_2);
    verify(listener, timeout(1000).times(2)).onEvent(eq(ModuleEvents.INSTALLED), any());
    verify(listener, timeout(1000).times(2)).onEvent(eq(ModuleEvents.INSTALLING), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
  }

  @Test
  void ensureSuccessfullyStartingMultiplePluginProducesSuccessEvent() {

    install(StandardModules.YAML);
    restart();
    kernel.addEventListener(listener, STARTING, STARTED, START_FAILED);

    install(TEST_PLUGIN_2, TEST_PLUGIN_1, TEST_PLUGIN_SPRING_DEP, TEST_PLUGIN_SPRING);
    startAndWait(4, "test-plugin-1", "test-plugin-2", "spring-plugin", "spring-plugin-dep");
    verify(listener, timeout(1000).times(4)).onEvent(eq(STARTING), any());
    verify(listener, timeout(1000).times(4)).onEvent(eq(STARTED), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(START_FAILED), any());
  }

  @Test
  void ensureSuccessfullyStartingSinglePluginProducesSuccessEvent() {
    install(StandardModules.YAML);
    restart();
    kernel.addEventListener(listener, STARTING, STARTED, START_FAILED);

    install(TEST_PLUGIN_1, TEST_PLUGIN_2);
    startAndWait(2, "test-plugin-1", "test-plugin-2");

    verify(listener, timeout(1000).times(2)).onEvent(eq(STARTING), any());
    verify(listener, timeout(1000).times(2)).onEvent(eq(STARTED), any());
    verify(listener, timeout(1000).times(0)).onEvent(eq(START_FAILED), any());
  }
}
