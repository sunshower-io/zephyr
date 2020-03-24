package io.sunshower.gyre;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CompactTrieMapTest {

  private CompactTrieMap<String, String, Object> map;

  @BeforeEach
  void setUp() {
    map = new CompactTrieMap<>(new RegexStringAnalyzer(":"));
  }

  Random random = new Random();

  String randomPath(int length) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int rand = random.nextInt(100);

      b.append(rand);
      if (i < length - 1) {
        b.append(":");
      }
    }
    return b.toString();
  }

  @Test
  void ensureToStringWorks() {

    map.put("hello:world", 1);
    map.put("sup:peeps", 2);

    String expected =
        "(k:(root) -> v:null)\n"
            + "├── (k:hello -> v:null)\n"
            + "│   └── (k:world -> v:1)\n"
            + "└── (k:sup -> v:null)\n"
            + "    └── (k:peeps -> v:2)\n";
    assertEquals(map.toString(), expected);
  }

  @Test
  void ensurePutAllWorks() {
    val a = new HashMap<String, Object>();
    a.put("hello:world", 1);
    a.put("sup:peeps", 2);

    map.putAll(a);

    assertEquals(map.size(), 2);
  }

  @Test
  void ensureContainsAllWorks() {
    val a = new HashMap<String, Object>();
    a.put("hello:world", 1);
    a.put("sup:peeps", 2);

    map.put("hello:world", 1);
    map.put("sup:peeps", 2);
    assertTrue(map.entrySet().containsAll(a.entrySet()));
  }

  @Test
  void ensureRemoveAllWorks() {
    val a = new HashMap<String, Object>();
    a.put("hello:world", 1);
    a.put("sup:peeps", 2);

    map.put("hello:world", 1);
    map.put("sup:peeps", 2);
    assertTrue(map.entrySet().removeAll(a.entrySet()));
    assertTrue(map.isEmpty());
  }

  @Test
  void ensureRemovingAllElementsWorks() {
    List<String> keys = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      val path = randomPath(10);
      map.put(path, i);
      keys.add(path);
    }

    for (val k : keys) {
      val v = map.get(k);
      int size = map.size();
      assertEquals(v, map.remove(k));
      assertEquals(size - 1, map.size());
    }
    assertTrue(map.isEmpty());
  }

  @Test
  void ensureRemovingNonExistingObjectProducesNull() {
    assertNull(map.remove(randomPath(100)));
  }

  @Test
  void ensureValueIteratorWorks() {

    for (int i = 0; i < 20; i++) {
      val path = randomPath(10);
      map.put(path, i);
    }

    map.put("hello:world", 100);

    val values = map.values();
    val viter = values.iterator();
    int count = 0;
    while (viter.hasNext()) {
      val next = viter.next();
      assertTrue(values.contains(next));
      count++;
    }
    assertEquals(21, count);
  }

  @Test
  void ensureLocatingDeeplyNestedKeyWorks() {
    map.put("1:2:3:4:5:6:7", 1);

    assertTrue(map.containsKey("1:2:3:4:5:6:7"));
    assertFalse(map.containsKey("1:2:3:4:5:6:7:8"));
  }

  @Test
  void ensureIteratorWorks() {
    map.put("1", "a");
    map.put("1:2", "b");
    map.put("1:3:4", "c");

    val e = map.keySet().iterator();
    val result = new HashSet<>(map.size());
    while (e.hasNext()) {
      result.add(e.next());
    }

    assertTrue(result.contains("1"));
    assertTrue(result.contains("1:2"));
    assertTrue(result.contains("1:3:4"));
  }

  @Test
  void ensureRemovingSingleValueWorks() {
    map.put("hello", 1);
    val result = map.remove("hello");
    assertEquals(1, result);
    assertEquals(0, map.size());
  }

  @Test
  void ensureLocatingDeeplyNestedValueWorks() {
    map.put("1:2:3:4:5:6", 1);
    map.put("1:2:3:4:5:6:8", 2);

    assertTrue(map.containsValue(2));
    assertFalse(map.containsValue(3));
  }

  @Test
  void ensureRedefiningValueResultsInCorrectSize() {
    map.put("hello:world", 1);
    assertEquals(1, map.size());
    map.put("hello:world", 2);
    assertEquals(1, map.size());
  }

  @Test
  void ensurePuttingSingleValueResultsInRetrievableValue() {
    map.put("hello", "world");
    assertEquals("world", map.get("hello"));
  }

  @Test
  void ensureAdditionResultsInLengthBeingIncremented() {
    for (int i = 0; i < 100; i++) {
      map.put("hello:world:what:up:t" + i, "value" + i);
    }
    map.put("hello:world:what:up:t34:subbeans", "value" + "noway");
  }

  @Test
  void ensureFrontierWorks() {
    for (int i = 0; i < 100; i++) {
      map.put("hello:world:what:up:t" + i, "value" + i);
    }

    val children = map.level("hello:world:what:up");
    assertEquals(children.size(), 100);
  }

  @Test
  void ensureKeySetContainsKey() {
    map.put("hello", "world");
    assertTrue(map.keySet().contains("hello"));
  }

  @Test
  void ensurePuttingSplitValueInTrieResultsInResultBeingRetrievable() {
    map.put("hello:world:how:are:you", "world");
    assertEquals("world", map.get("hello:world:how:are:you"));
  }
}
