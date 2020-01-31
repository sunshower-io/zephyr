package io.sunshower.kernel.test;

import io.zephyr.cli.Zephyr;
import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.Module;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.val;

public class ModuleLifecycleManager {
  private final Zephyr zephyr;

  public ModuleLifecycleManager(Zephyr zephyr) {
    this.zephyr = zephyr;
  }

  public void start(Predicate<Module> modules) {
    val matching =
        zephyr
            .getPlugins()
            .stream()
            .filter(modules)
            .map(Module::getCoordinate)
            .map(Coordinate::toCanonicalForm)
            .collect(Collectors.toList());
    zephyr.start(matching);
  }

  public void remove(Predicate<Module> o) {

    val matching =
        zephyr
            .getPlugins()
            .stream()
            .filter(o)
            .map(Module::getCoordinate)
            .map(Coordinate::toCanonicalForm)
            .collect(Collectors.toList());
    zephyr.remove(matching);
  }
}
