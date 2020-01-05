package io.zephyr.cli;

import java.util.List;

public interface History {

  List<Command> clear();

  List<Command> getHistory();

  List<Command> getHistory(int count);

  List<Command> getHistory(String match);
}
