package io.zephyr.api;

import lombok.Getter;

public class Query<T> {

  @Getter private final String query;
  @Getter private final Object context;
  @Getter private final String language;

  public Query(final String query, final String language, final Object context) {
    this.query = query;
    this.language = language;
    this.context = context;
  }
}
