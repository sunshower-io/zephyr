package io.sunshower.kernel.state.xml;

import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.state.Memento;
import io.sunshower.kernel.state.Originator;

public class CoordinateOriginator implements Originator<Coordinate> {

  @Override
  public Memento<Coordinate> save() {
    return null;
  }

  @Override
  public void restore(Memento<Coordinate> memento) {}
}
