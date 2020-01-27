package io.zephyr.api;

import io.zephyr.Context;
import lombok.Getter;

public class Query<T> {

  @Getter private final String query;
  @Getter private final String language;
  @Getter private final Context<T> context;

  public Query(final String query, final String language, final Context<T> context) {
    this.query = query;
    this.context = context;
    this.language = language;
  }
}
