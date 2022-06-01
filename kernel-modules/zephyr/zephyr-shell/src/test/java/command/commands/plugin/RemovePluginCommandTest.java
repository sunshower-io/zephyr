package command.commands.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.zephyr.kernel.modules.shell.ShellTestCase;
import org.junit.jupiter.api.Test;

public class RemovePluginCommandTest extends ShellTestCase {

  @Test
  void ensureInstallingAndRemovingPluginWorks() {
    try {
      installAndWaitForModuleCount(1, TestPlugins.TEST_PLUGIN_1);
      assertEquals(1, kernel.getModuleManager().getModules().size());
      remove("io.sunshower:test-plugin-1:1.0.0-SNAPSHOT");
      assertEquals(0, kernel.getModuleManager().getModules().size());
    } finally {
      removeAll();
    }
  }
}
