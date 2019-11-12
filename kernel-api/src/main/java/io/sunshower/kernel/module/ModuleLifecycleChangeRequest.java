package io.sunshower.kernel.module;

import io.sunshower.kernel.Coordinate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class ModuleLifecycleChangeRequest implements ModuleRequest {

  @Getter @Setter private Coordinate coordinate;
  @Getter @Setter private ModuleLifecycle.Actions lifecycleActions;
}
