package io.sunshower.kernel.shell;

public interface CommandRegistry {

  void register(String name, Object command);

  Object getCommand(String command);
}
