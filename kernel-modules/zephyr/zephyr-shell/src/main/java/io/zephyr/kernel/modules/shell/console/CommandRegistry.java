package io.zephyr.kernel.modules.shell.console;

import java.util.List;

public interface CommandRegistry {

  List<Command> getCommands();

  /**
   * @param command the command to register
   * @return the command that is replaced (if any) by this command
   */
  Command register(Command command);

  Command unregister(String name);

  Command resolve(String name);
}
