package io.zephyr.kernel.memento;

import io.zephyr.kernel.Coordinate;
import io.zephyr.kernel.CoordinateSpecification;
import lombok.val;

public class Mementos {

  public static void writeCoordinate(Memento result, Coordinate coordinate) {
    val coordinateMemento = result.child("coordinate");
    coordinateMemento.write("group", coordinate.getGroup());
    coordinateMemento.write("name", coordinate.getName());
    coordinateMemento.write("version", coordinate.getVersion());
  }

  public static void writeCoordinateSpecification(
      Memento result, CoordinateSpecification specification) {
    val specMemento = result.child("coordinate-specification");
    specMemento.write("group", specification.getGroup());
    specMemento.write("name", specification.getName());
    specMemento.write("version", specification.getVersionSpecification());
  }
}
