package io.zephyr;

import io.zephyr.api.ServiceReference;
import io.zephyr.kernel.Module;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Context<S> {

  private final Module module;
  private final ServiceReference<S> service;
}
