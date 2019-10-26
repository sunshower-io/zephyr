package io.sunshower.kernel.core;

import io.sunshower.kernel.Coordinate;
import java.util.List;

public interface ActionTree {

  int size();

  int height();

  List<Coordinate> getLevel(int level);
}
