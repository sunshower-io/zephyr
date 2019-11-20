package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;

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
