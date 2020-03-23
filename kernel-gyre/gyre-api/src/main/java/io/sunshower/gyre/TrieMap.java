package io.sunshower.gyre;

import java.util.List;
import java.util.Map;

public interface TrieMap<K, V> extends Map<K, V> {

  List<V> level(K key);
}
