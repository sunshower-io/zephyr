package io.sunshower.kernel;

public interface Coordinate extends Comparable<Coordinate> {

  String getName();

  String getGroup();

  Version getVersion();

  default String toCanonicalForm() {
    return String.format("%s:%s:%s", getGroup(), getName(), getVersion());
  }
}
