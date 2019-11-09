package io.sunshower.kernel.core;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartLifecycleAction implements LifecycleAction {
  final Module target;
  final Lifecycle.State state;
}
