package io.sunshower.kernel.module;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import lombok.Getter;
import lombok.NonNull;

public class ModuleLifecycle implements Lifecycle {
  private volatile State state;
  private final Module module;

  public enum Actions {
    // move to Resolved
    Stop,
    Delete(Stop), // remove
    Install, // move to resolved
    Resolve(Install), // move to resolved
    Activate(Install, Resolve); // move to active

    @Getter final Actions[] predecessors;

    Actions(Actions... predecessors) {
      this.predecessors = predecessors;
    }
  }

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
