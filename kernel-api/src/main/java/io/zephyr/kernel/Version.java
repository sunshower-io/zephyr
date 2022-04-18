package io.zephyr.kernel;

public interface Version extends Comparable<Version> {

  boolean satisfies(String range);

  default boolean satisfies(CoordinateSpecification coordinateSpecification) {
    return satisfies(coordinateSpecification.getVersionSpecification());
  }
}
