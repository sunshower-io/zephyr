package io.sunshower.gyre;

import java.util.Iterator;

public interface Analyzer<K, T> {
  Iterator<T> segments(K key);
}
