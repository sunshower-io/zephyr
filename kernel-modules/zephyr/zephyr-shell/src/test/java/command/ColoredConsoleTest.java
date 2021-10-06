package command;

import io.zephyr.kernel.modules.shell.command.ColoredConsole;
import io.zephyr.kernel.modules.shell.console.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@DisabledOnOs({OS.MAC, OS.WINDOWS})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class ColoredConsoleTest {

  private ColoredConsole console;

  @BeforeEach
  void setUp() {
    console = new ColoredConsole();
  }

  @Test
  void ensureOutputIsWhatWeExpect() {
    console.writeln(
        "hi wab I love you even though I suck",
        Color.colors(Color.BlueBackgroundBright, Color.CyanUnderlined));
  }
}
