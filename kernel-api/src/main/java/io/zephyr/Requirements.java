package io.zephyr;

public class Requirements {
  public static <T> Requirement<T> create(RequirementDefinition<T> requirement) {
    throw new UnsupportedOperationException("nope");
  }

  public static <T> Requirement<T> create(Class<T> type) {
    return null;
  }
}
