package io.zephyr.kernel.core;

import io.zephyr.kernel.concurrency.Process;
import java.util.concurrent.CompletionStage;

public interface KernelLifecycle {

  ClassLoader getLaunchClassloader();

  enum State {
    Error(-1),
    Stopped(0),
    Starting(1),
    Running(2),
    Stopping(3);

    final int state;

    State(final int state) {
      this.state = state;
    }
  }

  State getState();

  CompletionStage<Process<String>> stop();

  CompletionStage<Process<String>> start();

  CompletionStage<Process<String>> setState(State state);
}
