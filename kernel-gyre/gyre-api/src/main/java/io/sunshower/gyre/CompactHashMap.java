package io.sunshower.gyre;

import lombok.val;

import java.util.*;

@SuppressWarnings("unchecked")
public class CompactHashMap<K, V> implements Map<K, V> {

  private static final float DEFAULT_LOAD_FACTOR = 0.75F;

  private final float loadFactor;

  private Entry<K, V>[] table;

  /** the size of the table (including unfilled positions) */
  private final int size;

  /** the number of records filled */
  private int filled;

  /** */
  private int maxProbe;

  public CompactHashMap() {
    this(10);
  }

  public CompactHashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  public CompactHashMap(int initialCapacity, float loadFactor) {
    this.filled = 0;
    this.maxProbe = 0;
    this.size = initialCapacity;
    this.loadFactor = loadFactor;
    this.table = new Entry[initialCapacity];
  }

  @Override
  public int size() {
    return filled;
  }

  @Override
  public boolean isEmpty() {
    return filled == 0;
  }

  @Override
  public boolean containsKey(Object key) {

    val hashCode = codeFor(key);
    return table[hashCode] != null;
  }

  @Override
  public boolean containsValue(Object value) {
    for (val e : table) {
      if (e != null) {
        if (e.value == value || Objects.equals(e.value, value)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {

    val hashCode = key.hashCode();
    var i = hashCode & (table.length - 1);
    var probe = 0;

    while (probe <= maxProbe) {
      val current = table[i];
      if (current == null || current.probe < probe) {
        return null;
      }

      if (hashCode == current.hashcode
          && (key == current.key || Objects.equals(key, current.key))) {
        return current.value;
      }
      if (i == table.length - 1) {
        i = 0;
      } else {
        i = i + 1;
      }
      ++probe;
    }

    return null;
  }

  @Override
  public V put(K key, V value) {
    if (table.length * loadFactor < filled) {
      resize();
    }

    return insert(key, value, key.hashCode(), null);
  }

  @Override
  public V remove(Object key) {

    val hashcode = key.hashCode();
    int i = hashcode & (table.length - 1);
    int probe = 0;
    int index = 0;
    V result = null;

    while (probe <= maxProbe) {
      val current = table[i];

      if (current == null || current.probe < probe) {
        break;
      }

      if (hashcode == current.hashcode
          && (key == current.key || Objects.equals(key, current.key))) {
        index = i;
        result = current.value;
        break;
      }

      if (i == table.length - 1) {
        i = 0;
      } else {
        i = i + 1;
      }
      ++probe;
    }

    if (result == null) {
      return null;
    }
    filled = filled - 1;

    for (; ; ) {
      if (i == table.length - 1) {
        i = 0;
      } else {
        i = i + 1;
      }

      val current = table[i];
      if (current == null || current.probe == 0) {
        break;
      }
    }

    int idx = index;
    for (; ; ) {

      int j;
      if (idx == table.length - 1) {
        j = 0;
      } else {
        j = idx + 1;
      }

      if (j == i) {
        table[idx] = null;
        break;
      }

      val next = table[j];
      next.probe = next.probe - 1;
      table[idx] = next;
      idx = j;
    }

    return result;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    this.filled = 0;
    this.maxProbe = 0;
    this.table = new Entry[size];
  }

  @Override
  public Set<K> keySet() {
    return new ViewSet();
  }

  @Override
  public Collection<V> values() {
    return null;
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return null;
  }

  private int codeFor(Object key) {
    return key == null ? 0 : key.hashCode();
  }

  final class KeyIterator implements Iterator<K> {
    int cursor;
    int found;

    KeyIterator() {
      found = 0;
      cursor = 0;
    }

    @Override
    public boolean hasNext() {
      return found < filled;
    }

    @Override
    public K next() {

      if (found < filled) {
        int len = table.length;
        for (int i = cursor; i < len; i++) {
          val next = table[i];
          if (next != null) {
            found++;
            cursor = i + 1;
            return next.key;
          }
        }
      }
      throw new NoSuchElementException("No more elements");
    }
  }

  final class ViewSet implements Set<K> {

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean contains(Object o) {
      return false;
    }

    @Override
    public Iterator<K> iterator() {
      return new KeyIterator();
    }

    @Override
    public Object[] toArray() {
      return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return null;
    }

    @Override
    public boolean add(K k) {
      return false;
    }

    @Override
    public boolean remove(Object o) {
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return false;
    }

    @Override
    public void clear() {}
  }

  private V insert(K key, V value, int hashCode, Entry<K, V> cache) {
    V result = null;
    int probe = 0;
    int i = hashCode & (table.length - 1);
    for (; ; ) {

      val current = table[i];

      if (current == null) {
        if (cache != null) {
          table[i] = cache;
          cache.hashcode = hashCode;
          cache.key = key;
          cache.value = value;
          cache.probe = probe;
        } else {
          table[i] = new Entry<>(key, value, hashCode, probe);
        }

        ++filled;

        if (probe > maxProbe) {
          maxProbe = probe;
        }
        break;

      } else if (hashCode == current.hashcode
          && (key == current.key || Objects.equals(key, current.key))) {
        result = current.value;
        current.value = value;
        break;
      } else if (current.probe < probe) {
        if (probe > maxProbe) {
          maxProbe = probe;
        }

        val previousKey = current.key;
        val previousValue = current.value;
        val previousProbe = current.probe;
        val previousHash = current.hashcode;

        current.key = key;
        current.probe = probe;
        current.value = value;
        current.hashcode = hashCode;

        key = previousKey;
        hashCode = previousHash;
        value = previousValue;
        probe = previousProbe;
      }

      if (i == table.length - 1) {
        i = 0;
      } else {
        i = i + 1;
      }
      ++probe;
    }
    return result;
  }

  private void resize() {
    val previousTable = table;
    val previousLength = table.length;
    table = new Entry[previousTable.length * 2];
    maxProbe = 0;
    filled = 0;

    int count = 0;
    while (count < previousLength) {
      val e = previousTable[count++];
      if (e != null) {
        insert(e.key, e.value, e.hashcode, e);
      }
    }
  }

  static final class Entry<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;
    private int probe;
    private int hashcode;

    Entry(K key) {
      this(key, null);
    }

    Entry(K key, V value) {
      this(key, value, 0, 0);
    }

    Entry(K key, V value, int hashcode, int probe) {
      this.key = key;
      this.value = value;
      this.probe = probe;
      this.hashcode = hashcode;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return value;
    }

    @Override
    public V setValue(V value) {
      val result = this.value;
      this.value = value;
      return result;
    }
  }
}
