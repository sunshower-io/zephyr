package io.sunshower.kernel;

import io.sunshower.kernel.graph.Dependency;

import java.util.List;

public interface KernelExtensionDescriptor {

    Coordinate getCoordinate();

    List<Dependency> getDependencies();
}
