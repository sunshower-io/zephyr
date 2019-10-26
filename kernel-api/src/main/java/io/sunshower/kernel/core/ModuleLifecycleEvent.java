package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModuleLifecycleEvent {
  private final Module source;
  private final Lifecycle.State state;
}
