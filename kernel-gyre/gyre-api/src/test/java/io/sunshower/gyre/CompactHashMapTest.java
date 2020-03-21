package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompactHashMapTest {
  Map<Integer, Integer> map;

  @BeforeEach
  void setUp() {
    map = new CompactHashMap<>();
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
  void ensureKeyIteratorWorksForMultipleValues() {
    map.put(1, 2);
    map.put(2, 3);
    val ks = map.keySet().iterator();
    val set = new HashSet<>();
    while(ks.hasNext()) {
      set.add(ks.next());
    }
    assertTrue(set.contains(1));
    assertTrue(set.contains(2));
  }
}
