package io.zephyr.kernel.modules.shell.command;

import io.zephyr.kernel.modules.shell.console.CommandContext;
import io.zephyr.kernel.modules.shell.console.Result;

public class DefaultCommand extends AbstractCommand {
  private static final long serialVersionUID = 5240261956667412465L;

  public DefaultCommand(String name) {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {
    return null;
  }
}
