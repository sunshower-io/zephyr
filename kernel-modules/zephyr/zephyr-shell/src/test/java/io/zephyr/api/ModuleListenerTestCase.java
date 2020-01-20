package io.zephyr.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.zephyr.kernel.modules.shell.ShellTestCase;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ModuleListenerTestCase extends ShellTestCase {

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

  @Test
  void ensurePluginInstallingDispatchesEvent() {
    perform(
        () -> {
          kernel.addEventListener(listener, ModuleEvents.INSTALLING);
        },
        () -> {
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALLING), any());
        },
        StandardModules.YAML);
  }

  @Test
  void ensureAttemptingToInstallInvalidPluginDispatchesFailedEvent() {
    perform(
        () -> {
          kernel.addEventListener(listener, ModuleEvents.INSTALL_FAILED, ModuleEvents.INSTALLED);
        },
        () -> {
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
        },
        new FileInstallable(new File("nothere")));
  }

  @Test
  void ensureSuccessfullyInstallingModuleProducesSucceededEvent() {
    perform(
        () -> {
          kernel.addEventListener(listener, ModuleEvents.INSTALLED, ModuleEvents.INSTALL_FAILED);
        },
        () -> {
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
        },
        StandardModules.YAML);
  }

  @Test
  void ensureSuccessfullyInstallingPluginProducesSucceededEvent() {
    installFully(StandardModules.YAML);
    perform(
        () -> {
          kernel.addEventListener(
              listener,
              ModuleEvents.INSTALLED,
              ModuleEvents.INSTALL_FAILED,
              ModuleEvents.INSTALLING);
        },
        () -> {
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALLED), any());
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALLING), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
        },
        TestPlugins.TEST_PLUGIN_1);
  }

  @Test
  void ensureSuccessfullyInstallingMultiplePluginsProducesSucceededEvent() {
    installFully(StandardModules.YAML);
    perform(
        () -> {
          kernel.addEventListener(
              listener,
              ModuleEvents.INSTALLED,
              ModuleEvents.INSTALL_FAILED,
              ModuleEvents.INSTALLING);
        },
        () -> {
          verify(listener, times(2)).onEvent(eq(ModuleEvents.INSTALLED), any());
          verify(listener, times(2)).onEvent(eq(ModuleEvents.INSTALLING), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
        },
        TestPlugins.TEST_PLUGIN_1,
        TestPlugins.TEST_PLUGIN_2);
  }

  @Test
  void ensureSuccessfullyStartingMultiplePluginProducesSuccessEvent() {

    installFully(StandardModules.YAML);
    perform(
        () -> {},
        () -> {
          kernel.addEventListener(
              listener, ModuleEvents.STARTING, ModuleEvents.STARTED, ModuleEvents.START_FAILED);
          startAndWait(4, "test-plugin-1", "test-plugin-2", "spring-plugin", "spring-plugin-dep");
          verify(listener, times(4)).onEvent(eq(ModuleEvents.STARTING), any());
          verify(listener, times(4)).onEvent(eq(ModuleEvents.STARTED), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.START_FAILED), any());
        },
        TestPlugins.TEST_PLUGIN_1,
        TestPlugins.TEST_PLUGIN_2,
        TestPlugins.TEST_PLUGIN_SPRING,
        TestPlugins.TEST_PLUGIN_SPRING_DEP);
  }

  @Test
  void ensureSuccessfullyStartingSinglePluginProducesSuccessEvent() {

    installFully(StandardModules.YAML);
    perform(
        () -> {},
        () -> {
          kernel.addEventListener(
              listener, ModuleEvents.STARTING, ModuleEvents.STARTED, ModuleEvents.START_FAILED);
          startAndWait(2, "test-plugin-1", "test-plugin-2");
          verify(listener, times(2)).onEvent(eq(ModuleEvents.STARTING), any());
          verify(listener, times(2)).onEvent(eq(ModuleEvents.STARTED), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.START_FAILED), any());
        },
        TestPlugins.TEST_PLUGIN_1,
        TestPlugins.TEST_PLUGIN_2);
  }

  protected void perform(Runnable before, Runnable after, Installable... modules) {
    start();
    try {
      before.run();
      installAndWaitForModuleCount(modules.length, modules);
      restart();
      after.run();
    } finally {
      kernel.removeEventListener(listener);
      stop();
    }
  }
}
