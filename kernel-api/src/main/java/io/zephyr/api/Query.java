package io.zephyr.api;

public class Query<T> {

  private final String query;
  private final Object context;
  private final String language;

  public Query(final String query, final String language, final Object context) {
    this.query = query;
    this.language = language;
    this.context = context;
  }
}
