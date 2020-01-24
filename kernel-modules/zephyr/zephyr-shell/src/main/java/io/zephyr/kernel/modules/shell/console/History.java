package io.zephyr.kernel.modules.shell.console;

import java.util.List;

public interface History {

  List<Command> clear();

  List<Command> getHistory();

  List<Command> getHistory(int count);

  List<Command> getHistory(String match);
}
