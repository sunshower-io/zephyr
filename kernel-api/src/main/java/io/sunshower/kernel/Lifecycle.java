package io.sunshower.kernel;

public interface Lifecycle {

  /**
   * This does not trigger a ModuleLifecycleChangedEvent (use the other methods for that)
   *
   * @param resolved
   */
  void setState(State resolved);

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
