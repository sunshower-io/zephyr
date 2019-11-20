package io.zephyr.api;

import java.io.Serializable;

public interface Command extends Serializable {

  String getName();

  Result execute(CommandContext context);
}
