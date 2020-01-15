package io.zephyr.kernel.extensions;

import lombok.val;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public interface EntryPointRegistry {

  default <T> T resolveService(Class<T> type) {
    val maybeResult = getEntryPoints().stream().filter(t -> t.exports(type)).findAny();
    return maybeResult.map(entryPoint -> entryPoint.getService(type)).orElse(null);
  }

  @SuppressWarnings("unchecked")
  default <T extends EntryPoint> T resolveEntryPoint(Predicate<EntryPoint> filter) {
    return (T) getEntryPoints(filter).get(0);
  }

  List<EntryPoint> getEntryPoints();

  List<EntryPoint> getEntryPoints(Predicate<EntryPoint> filter);
}
