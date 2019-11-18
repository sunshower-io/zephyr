package io.zephyr.kernel.shell;

import static io.zephyr.kernel.shell.Color.colors;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ShellConsoleTest {

  @Test
  void ensureColorSyntaxWorks() {
    assertNotNull(colors(Color.Black, Color.BlackBackground), "must alias back to array correctly");
  }
}
