package io.sunshower.kernel.shell;

import io.sunshower.gyre.Pair;

import java.util.List;

public interface CommandRegistry {
  List<Pair<String, Object>> getCommands();

  void register(String name, Object command);

  Object getCommand(String command);
}
