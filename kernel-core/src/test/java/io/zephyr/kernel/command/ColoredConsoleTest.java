package io.zephyr.kernel.command;

import io.zephyr.cli.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
