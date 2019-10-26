package io.sunshower.kernel.core.actions;

import io.sunshower.kernel.Lifecycle;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ActionTree;
import io.sunshower.kernel.core.LifecycleAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartLifecycleAction implements LifecycleAction {
  final Module target;
  final Lifecycle.State state;
  final ActionTree actionTree;
}
