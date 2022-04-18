package io.zephyr.kernel.events;

import io.sunshower.lang.events.Event;
import io.zephyr.kernel.status.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class KernelEvents {

  public static <T> Event<T> create(T source) {
    return new DEvent<>(source, null);
  }

  public static <T> Event<T> createWithStatus(Status status) {
    return new DEvent<>(null, status);
  }

  public static <T> Event<T> create(T source, Status status) {
    return new DEvent<>(source, status);
  }
}

@Getter
@AllArgsConstructor
final class DEvent<T> implements Event<T> {

  final T target;
  final Status status;
}
