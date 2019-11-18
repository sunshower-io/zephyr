package io.zephyr.kernel.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class Events {

  public static <T> Event<T> create(T source) {
    return new DEvent<>(source);
  }
}

@Getter
@AllArgsConstructor
final class DEvent<T> implements Event<T> {

  final T target;
}
