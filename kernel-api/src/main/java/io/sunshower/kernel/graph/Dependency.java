package io.sunshower.kernel.graph;

import io.sunshower.kernel.Coordinate;

public interface Dependency {

    boolean isRequired();

    Coordinate getCoordinate();
}
