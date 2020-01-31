package io.zephyr.api;

import io.zephyr.Context;

public class Queries {

  public static <T> Query<T> create(String lang, String filter) {
    return new Query<>(filter, lang, Context.empty());
  }
}
