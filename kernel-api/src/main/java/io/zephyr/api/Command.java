package io.zephyr.api;

public interface Command {

  String getName();

  Result execute(CommandContext context);
}
