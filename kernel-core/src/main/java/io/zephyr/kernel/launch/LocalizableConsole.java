package io.zephyr.kernel.launch;

import io.zephyr.kernel.shell.ShellConsole;
import java.util.ResourceBundle;
import lombok.experimental.Delegate;

public class LocalizableConsole implements ShellConsole, AutoCloseable {
  @Delegate private final ShellConsole delegate;
  private final ResourceBundle resourceBundle;

  public LocalizableConsole(ShellConsole console, Class<?> type) {
    delegate = console;
    this.resourceBundle = ResourceBundle.getBundle("i18n." + type.getName());
  }

  @Override
  public void close() throws Exception {}
}
