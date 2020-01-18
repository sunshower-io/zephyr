package command.commands.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.zephyr.kernel.modules.shell.ShellTestCase;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ListPluginCommandTest extends ShellTestCase {

  @Test
  void ensurePluginsAreInstalledCorrectly() {
    installAndWaitForModuleCount(2, TestPlugins.TEST_PLUGIN_1, TestPlugins.TEST_PLUGIN_2);
    assertEquals(2, kernel.getModuleManager().getModules().size(), "must have 2 modules installed");
  }
}
