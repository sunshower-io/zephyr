package io.sunshower.kernel.test;

import io.zephyr.kernel.Module;
import java.util.Arrays;
import java.util.function.Predicate;

public class ModuleFilters {

  public static final Predicate<Module> named(String... names) {
    return module -> Arrays.stream(names).anyMatch(t -> module.getCoordinate().getName().equals(t));
  }
}
