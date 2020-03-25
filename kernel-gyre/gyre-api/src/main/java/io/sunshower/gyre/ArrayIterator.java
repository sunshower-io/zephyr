package io.sunshower.gyre;

import java.util.Iterator;
import java.util.Objects;

public final class ArrayIterator<T> implements Iterator<T> {
  private int count;
  private final T[] store;

  public ArrayIterator(T[] store) {
    Objects.requireNonNull(store);
    this.store = store;
  }

  @Override
  public boolean hasNext() {
    return count < store.length;
  }

  @Override
  public T next() {
    return store[count++];
  }
}
