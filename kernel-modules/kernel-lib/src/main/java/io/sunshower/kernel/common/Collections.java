package io.sunshower.kernel.common;

import java.util.ArrayList;
import java.util.List;

public class Collections {
  private Collections() {}

  public static <K, T> List<T> newList(K arg) {
    return new ArrayList<>();
  }
}
