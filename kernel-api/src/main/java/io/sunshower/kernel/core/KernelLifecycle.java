package io.sunshower.kernel.core;

import io.sunshower.kernel.concurrency.TaskTracker;

public interface KernelLifecycle {

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

  TaskTracker<String> stop();

  TaskTracker<String> start();

  TaskTracker<String> setState(State state);
}
