package io.zephyr.api;

public interface Command {

  String getName();

  Result invoke(CommandContext context);
}
