package io.sunshower.kernel;

public interface Coordinate extends Comparable<Coordinate> {

  String getName();

  String getGroup();

  Version getVersion();
}
