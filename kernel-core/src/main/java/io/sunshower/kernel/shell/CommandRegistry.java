package io.sunshower.kernel.shell;

import io.sunshower.gyre.Pair;
import java.util.List;

public interface CommandRegistry {
  List<Pair<String, Command>> getCommands();

  void register(String name, Command command);

  Command getCommand(String command);
}
