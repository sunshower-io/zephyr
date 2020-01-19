package io.zephyr.api;

import io.zephyr.kernel.Module;
import io.zephyr.kernel.events.Event;
import io.zephyr.kernel.modules.shell.ShellTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ModuleListenerTestCase extends ShellTestCase {

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
          kernel.addEventListener(listener, ModuleEvents.INSTALL_FAILED);
        },
        () -> {
          verify(listener, times(1)).onEvent(eq(ModuleEvents.INSTALL_FAILED), any());
          verify(listener, times(0)).onEvent(eq(ModuleEvents.INSTALLED), any());
        },
        new FileInstallable(new File("nothere")));
  }

  protected void perform(Runnable before, Runnable after, Installable... modules) {
    start();
    try {
      before.run();
      install(modules);
      restart();
      after.run();
    } finally {
      kernel.removeEventListener(listener);
      stop();
    }
  }
}
