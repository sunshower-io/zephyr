package io.zephyr.kernel.command;

import io.zephyr.api.Command;
import lombok.Getter;
import lombok.NonNull;

public abstract class AbstractCommand implements Command {
  @Getter final String name;

  protected AbstractCommand(@NonNull String name) {
    this.name = name;
  }
}
