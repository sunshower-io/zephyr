package io.sunshower.kernel.core;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.val;

public class DefaultModuleContext implements ModuleContext {
  private final List<ModuleLifecycleListener> listeners;

  public DefaultModuleContext() {
    listeners = new ArrayList<>();
  }

  void dispatch(ModuleLifecycleEvent event) {
    synchronized (listeners) {
      for (val listener : listeners) {
        listener.moduleLifecycleChanged(event);
      }
    }
  }

  @Override
  public void addModuleLifecycleListener(@NonNull ModuleLifecycleListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeModuleLifecycleListener(@NonNull ModuleLifecycleListener listener) {
    listeners.remove(listener);
  }
}
