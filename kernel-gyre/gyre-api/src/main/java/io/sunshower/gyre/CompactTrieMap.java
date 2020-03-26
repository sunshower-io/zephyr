package io.sunshower.gyre;

import java.util.*;
import lombok.val;

public class CompactTrieMap<K, T, V> extends AbstractTrieMap<K, T, V> {

  public CompactTrieMap(Analyzer<K, T> analyzer) {
    super(analyzer);
  }

  @Override
  protected Entry<K, T, V> createRoot() {
    return new ListBasedEntry<>();
  }

  static final class ListBasedEntry<K, T, V> extends AbstractTrieMap.Entry<K, T, V> {
    private int index;
    private List<ListBasedEntry<K, T, V>> children;

    protected ListBasedEntry() {
      super();
    }

    protected ListBasedEntry(K key, T identity, V value) {
      super(key, identity, value);
    }

    @Override
    public void clear() {
      children = null;
    }

    @Override
    public Entry<K, T, V> create(K key, T seg) {
      val result = new ListBasedEntry<K, T, V>(key, seg, null);
      if (children == null) {
        children = new ArrayList<>();
      }
      children.add(result);
      result.index = children.size() - 1;
      return result;
    }

    @Override
    public Entry<K, T, V> locate(K key, T seg) {
      if (children == null) {
        return null;
      }
      for (val child : children) {
        val id = child.identity;
        if (id == seg || Objects.equals(id, seg)) {
          return child;
        }
      }
      return null;
    }

    @Override
    public V remove(Entry<K, T, V> current) {
      val d = (ListBasedEntry<K, T, V>) current;
      val child = children.get(d.index);
      val result = d.value;

      if (child.children == null) {
        children.remove(d.index);
        for (int i = d.index; i < children.size(); i++) {
          children.get(i).index -= 1;
        }
      } else {
        d.value = null;
        d.internal = true;
      }
      return result;
    }

    @Override
    public List<V> getChildValues() {
      if (children == null) {
        return Collections.emptyList();
      }

      val result = new ArrayList<V>(children.size());
      for (val c : children) {
        result.add(c.value);
      }
      return result;
    }

    @Override
    public Iterator<? extends Entry<K, T, V>> iterator() {
      if (children == null) {
        return Collections.emptyIterator();
      }

      return children.iterator();
    }
  }
}
