package io.zephyr.kernel.modules.shell.console;

import java.io.Serializable;

public interface Command extends Serializable {

  String getName();

  Result execute(CommandContext context);
}
