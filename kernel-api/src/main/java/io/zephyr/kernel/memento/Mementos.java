package io.zephyr.kernel.memento;

import io.zephyr.kernel.Coordinate;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

public class Mementos {


  public static void writeCoordinate(Memento result, Coordinate coordinate) {
    val coordinateMemento = result.child("coordinate");
    coordinateMemento.write("group", coordinate.getGroup());
    coordinateMemento.write("name", coordinate.getName());
    coordinateMemento.write("version", coordinate.getVersion());
  }
}
