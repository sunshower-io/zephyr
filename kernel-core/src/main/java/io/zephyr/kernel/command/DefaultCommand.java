package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;

public class DefaultCommand extends AbstractCommand {
  public DefaultCommand(String name) {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {
    return null;
  }
}
