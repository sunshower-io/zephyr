package io.sunshower.kernel;

import java.io.File;
import java.net.URL;
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

  URL getSource();

  File getLoadedFile();

  File getExtensionDirectory();

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
