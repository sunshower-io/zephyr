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

  @SuppressWarnings("unchecked")
  public static final <S> Context<S> empty() {
    return (Context<S>) EMPTY;
  }

  static final Context<?> EMPTY = new Context<>(null, null);
}
