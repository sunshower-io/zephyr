package command.commands.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.test.common.Tests;
import io.zephyr.kernel.modules.shell.ShellTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ListPluginCommandTest extends ShellTestCase {


  @Test
  void ensurePluginsAreInstalledCorrectly() {
    installAndWaitForModuleCount(2, TestPlugins.TEST_PLUGIN_1, TestPlugins.TEST_PLUGIN_2);
    assertTrue(kernel.getModuleManager().getModules().size() > 0, "must have 2 modules installed");
  }
}
