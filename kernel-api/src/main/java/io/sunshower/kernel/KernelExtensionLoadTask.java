package io.sunshower.kernel;

import java.util.concurrent.CompletableFuture;

public interface KernelExtensionLoadTask<
    T extends KernelExtensionDescriptor, U extends KernelExtensionLoadTask<T, U>> {

  enum State {
    Unstarted,
    Paused,
    Completed,
    Error,
    Cancelled,
    Running
  }

  void start();

  boolean isComplete();

  void pause();

  void resume();

  U restart() throws KernelExtensionConflictException;

  void cancel();

  State getState();

  Throwable getError();

  CompletableFuture<T> getFuture();
}
