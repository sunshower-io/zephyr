package io.sunshower.kernel.launch;

import io.sunshower.kernel.shell.ShellConsole;
import lombok.experimental.Delegate;

import java.util.ResourceBundle;

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
