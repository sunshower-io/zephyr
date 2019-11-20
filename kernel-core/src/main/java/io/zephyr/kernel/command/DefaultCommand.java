package io.zephyr.kernel.command;

import io.zephyr.api.CommandContext;
import io.zephyr.api.Result;
import lombok.NonNull;

public class DefaultCommand extends AbstractCommand {
  public DefaultCommand(@NonNull String name) {
    super(name);
  }

  @Override
  public Result execute(CommandContext context) {
    return null;
  }
}
