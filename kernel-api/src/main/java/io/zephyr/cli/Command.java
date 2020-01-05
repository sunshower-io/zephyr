package io.zephyr.cli;

import java.io.Serializable;

public interface Command extends Serializable {

  String getName();

  Result execute(CommandContext context);
}
