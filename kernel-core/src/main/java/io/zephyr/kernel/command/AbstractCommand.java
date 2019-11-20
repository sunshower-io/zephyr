package io.zephyr.kernel.command;

import io.zephyr.api.Command;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractCommand implements Command {
  private static final long serialVersionUID = 8379992772036944184L;
  @Getter final String name;

  protected AbstractCommand(@NonNull String name) {
    this.name = name;
  }
}
