package io.sunshower.kernel.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

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

  CompletableFuture<Void> stop();
  CompletableFuture<Void> start();

  CompletableFuture<Void> setState(State state);
}
