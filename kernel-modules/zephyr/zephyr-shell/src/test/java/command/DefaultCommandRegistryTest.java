package command;

import static org.junit.jupiter.api.Assertions.*;

import io.zephyr.kernel.modules.shell.command.AbstractCommand;
import io.zephyr.kernel.modules.shell.command.DefaultCommandRegistry;
import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.CommandRegistry;
import io.zephyr.kernel.modules.shell.console.Result;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestsShouldIncludeAssert",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class DefaultCommandRegistryTest {

  CommandRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new DefaultCommandRegistry();
  }

  @Test
  void ensureRegistryBeginsEmpty() {
    assertEquals(registry.getCommands().size(), 0, "commands must initially be empty");
  }

  @Test
  void ensureRegisteringCommandResultsInCommandAppearingInCommandList() {
    val command = new TCommand("whatever");
    registry.register(command);
    assertTrue(
        registry.getCommands().contains(command), "registry command list must contain commands");
  }

  @Test
  void ensureResolvingRegisteredCommandWorks() {
    val command = new TCommand("frapper");
    registry.register(command);
    assertEquals(registry.resolve("frapper"), command, "registry must contain command");
  }

  @Test
  void ensureUnregisteringCommandWorks() {
    val command = new TCommand("frapper");
    registry.register(command);
    assertEquals(registry.resolve("frapper"), command, "registry must contain command");
    registry.unregister(command.getName());
    assertNull(registry.resolve("frapper"), "registry must not contain command");
  }

  static class TCommand extends AbstractCommand {

    private static final long serialVersionUID = 6792450710392777687L;

    protected TCommand(@NonNull String name) {
      super(name);
    }

    @Override
    public Result execute(CommandContext context) {
      return null;
    }
  }
}
