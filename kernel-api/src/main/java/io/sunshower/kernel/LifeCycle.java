package io.sunshower.kernel;

public interface LifeCycle {
  enum State {
    Installed,
    Resolved,
    Uninstalled,
    Starting,
    Active,
    Stopping,
  }

  Module getModule();

  State getState();

  void reinstall();

  void uninstall();

  void start();

  void stop();

  void restart();
}
