package io.sunshower.kernel;

import lombok.NonNull;

public class ModuleLifecycle implements Lifecycle {
  private State state;
  private final Module module;

  public ModuleLifecycle(@NonNull final Module module) {
    this.module = module;
  }

  @Override
  public void setState(State resolved) {
    this.state = resolved;
  }

  @Override
  public Module getModule() {
    return module;
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public void reinstall() {}

  @Override
  public void uninstall() {}

  @Override
  public void start() {}

  @Override
  public void stop() {}

  @Override
  public void restart() {}
}
