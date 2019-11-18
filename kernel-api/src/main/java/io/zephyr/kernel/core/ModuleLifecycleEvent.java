package io.zephyr.kernel.core;

import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModuleLifecycleEvent {
  private final Module          source;
  private final Lifecycle.State state;
}
