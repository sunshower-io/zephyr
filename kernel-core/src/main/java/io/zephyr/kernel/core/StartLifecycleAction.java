package io.zephyr.kernel.core;

import io.zephyr.kernel.Lifecycle;
import io.zephyr.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartLifecycleAction implements LifecycleAction {
  final Module target;
  final Lifecycle.State state;
}
