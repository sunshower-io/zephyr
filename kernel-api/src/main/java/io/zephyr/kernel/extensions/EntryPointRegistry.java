package io.zephyr.kernel.extensions;

import java.util.List;
import java.util.function.Predicate;

public interface EntryPointRegistry {
  List<EntryPoint> getEntryPoints();

  List<EntryPoint> getEntryPoints(Predicate<EntryPoint> filter);
}
