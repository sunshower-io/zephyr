package io.sunshower.gyre;

import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class NodeFilters {

  static final class AcceptAll implements Predicate<Object> {

    @Override
    public boolean test(Object o) {
      return true;
    }
  }

  static final Predicate<?> acceptAll = new AcceptAll();

  public static <T> Predicate<T> acceptAll() {
    return (Predicate<T>) acceptAll;
  }
}
