package io.zephyr.kernel.module;

import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ModuleLifecycle implements Lifecycle {
  private final Module module;
  private volatile State state;

  public enum Actions {
    // move to Resolved
    Stop,
    Delete(Stop), // remove
    Install, // move to resolved
    Resolve(Install), // move to resolved
    Activate(Install, Resolve); // move to active

    public boolean isAtLeast(Actions actions) {
      if (actions == this) {
        return true;
      }
      for (val predecessor : predecessors) {
        if (predecessor == actions) {
          return true;
        }
      }
      return false;
    }

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
