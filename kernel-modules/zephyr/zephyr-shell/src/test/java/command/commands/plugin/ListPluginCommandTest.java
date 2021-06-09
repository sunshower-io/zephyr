package command.commands.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.zephyr.kernel.modules.shell.ShellTestCase;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ListPluginCommandTest extends ShellTestCase {

  @Test
  void ensurePluginsAreInstalledCorrectly() {
    installAndWaitForModuleCount(2, TestPlugins.TEST_PLUGIN_1, TestPlugins.TEST_PLUGIN_2);
    val modulesNamed =
        kernel.getModuleManager().getModules().stream()
            .filter(
                t -> {
                  val coord = t.getCoordinate();
                  return coord.getName().equals("test-plugin-1")
                      || coord.getName().equals("test-plugin-2");
                })
            .count();
    /** not sure why mac picks stuff up */
    assertEquals(2, modulesNamed, "must have 2 modules  installed");
  }
}
