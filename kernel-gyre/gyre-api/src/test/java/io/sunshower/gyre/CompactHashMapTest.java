package io.sunshower.gyre;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompactHashMapTest {
  Map<Integer, Integer> map;

  @BeforeEach
  void setUp() {
    map = new CompactHashMap<>();
  }

  @Test
  void ensureRemovingManyCollisionsWorks() {
    val map = createMap(1);

    val collisions = new ArrayList<Collider>();
    for (int i = 0; i < 1000; i++) {
      val collider = new Collider(i);
      collisions.add(collider);
      map.put(collider, i);
    }

    val liter = collisions.listIterator(collisions.size());
    while (liter.hasPrevious()) {
      val prev = liter.previous();
      assertEquals(prev.value, map.remove(prev));
      liter.remove();
    }

    assertTrue(map.isEmpty());
  }

  @Test
  void ensureMapItemCanBeRetrieved() {
    for (int i = 0; i < 100; i++) {
      map.put(i, i + 1);
    }

    for (int i = 0; i < 100; i++) {
      assertEquals(i + 1, map.get(i));
    }
  }


  @Test
  void ensureResizingFromZeroLengthWorks() {
    val map = createMap(0);
    map.put("1", "2");
  }

  @Test
  void ensureKeysetWorks() {
    val map = createMap(0);
    map.put("hello", "world");
    map.put("sup", "world");
    assertEquals(map.keySet(), Set.of("hello", "sup"));
  }

  @Test
  void ensureRemoveWorks() {
    map.put(1, 2);
    assertEquals(2, map.get(1));
    map.remove(1);
    assertNull(map.get(1));
  }

  @Test
  void ensureKeyIteratorWorksForEmptyMap() {
    assertFalse(map.keySet().iterator().hasNext());
  }

  @Test
  void ensureKeyIteratorWorksForSingleValue() {
    map.put(1, 2);
    val ks = map.keySet().iterator();
    assertTrue(ks.hasNext());
    assertEquals(ks.next(), 1);
    assertFalse(ks.hasNext());
  }

  @Test
  void ensureContainsWorksAfterRemoval() {
    map.put(1, 1);
    assertTrue(map.containsKey(1));
    map.remove(1);
    assertFalse(map.containsKey(1));
  }

  @Test
  void ensureContainsKeyFunctionsForCollision() {
    val fst = new Collider(1);
    val snd = new Collider(2);
    val map = createMap();
    map.put(fst, "hello");
    map.put(snd, "world");

    assertTrue(map.containsKey(fst));
    assertTrue(map.containsKey(snd));

    map.remove(snd);
    assertTrue(map.containsKey(fst));
    assertFalse(map.containsKey(snd));
  }

  @Test
  void ensureRemovingElementsWorks() {
    for (int i = 0; i < 100; i++) {
      map.put(i, 10 * i + i);
    }

    for (int i = 99; i >= 0; i--) {
      assertEquals(10 * i + i, map.remove(i));
    }

    assertTrue(map.isEmpty());
  }

  @Test
  void ensureInsertingAMillionElementsWorks() {
    long t1 = System.currentTimeMillis();
    int size = 1000 * 1000;
    val map = createMap(10);
    for (int i = 0; i < size; i++) {
      map.put(i, i + 1);
    }
    System.out.println(map.size());
    long t2 = System.currentTimeMillis();

    System.out.println("Elapsed time: " + (t2 - t1));

    val nmap = new HashMap<Integer, Integer>();

    t1 = System.currentTimeMillis();
    for (int i = 0; i < size; i++) {
      nmap.put(i, i + 1);
    }
    t2 = System.currentTimeMillis();

    System.out.println("Elapsed time: " + (t2 - t1));
  }

  @Test
  void ensureKeyIteratorWorksForMultipleValues() {
    map.put(1, 2);
    map.put(2, 3);
    val ks = map.keySet().iterator();
    val set = new HashSet<>();
    while (ks.hasNext()) {
      set.add(ks.next());
    }
    assertTrue(set.contains(1));
    assertTrue(set.contains(2));
  }

  static class Collider {
    int value;

    Collider(int value) {
      this.value = value;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }

      if (obj.getClass().equals(Collider.class)) {
        return ((Collider) obj).value == value;
      }
      return false;
    }
  }

  protected <K, V> Map<K, V> createMap() {
    return createMap(10);
  }

  protected <K, V> Map<K, V> createMap(int capacity) {
    return new CompactHashMap<>(capacity);
  }
}
