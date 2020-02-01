package io.zephyr.scan;

import io.zephyr.kernel.extensions.EntryPoint;
import io.zephyr.kernel.extensions.EntryPointRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MockEndpointRegistry implements EntryPointRegistry {

  final List<EntryPoint> entryPoints = new ArrayList<>();

  public void addEntryPoint(EntryPoint e) {
    this.entryPoints.add(e);
  }

  @Override
  public List<EntryPoint> getEntryPoints() {
    return entryPoints;
  }

  @Override
  public List<EntryPoint> getEntryPoints(Predicate<EntryPoint> filter) {
    return entryPoints.stream().filter(filter).collect(Collectors.toList());
  }
}
