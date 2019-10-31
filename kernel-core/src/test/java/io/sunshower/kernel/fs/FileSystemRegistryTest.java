package io.sunshower.kernel.fs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.file.FileSystem;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class FileSystemRegistryTest {

  private FileSystemRegistry registry;

  @BeforeEach
  public void setUp() {
    registry = new FileSystemRegistry();
  }

  @Test
  void ensureRemovalWorks() {
    val fs = mock(FileSystem.class);
    registry.add("a", fs);
    assertEquals(registry.size(), 1, "registry size must increment");
    assertSame(registry.remove("a"), fs, "removed filesystem must be the desired one");
    assertEquals(registry.size(), 0, "registry size must decrement");
    assertNull(registry.get("a"), "registry must not have value");
  }

  @Test
  void ensureRegistryDoesNotContainInvalidSegments() {
    val fs = mock(FileSystem.class);
    registry.add("kernel", fs);

    assertFalse(
        registry.contains(
            new String[] {"io", "sunshower", "kernel", "kernel-lib", "1.0.0-SNAPSHOT"}),
        "registry must not contain entry that does not exist");
  }

  @Test
  void ensureRegistryContainsValidSegments() {
    val fs = mock(FileSystem.class);
    val key = new String[] {"io", "sunshower", "kernel", "kernel-lib", "1.0.0-SNAPSHOT"};
    registry.add(key, fs);
    assertTrue(registry.contains(key), "registry must contain entry that exists");
  }

  @Test
  void ensureInsertingIntoRegistryWorks() {
    val fs = mock(FileSystem.class);
    assertNull(registry.add("com.whatever.bean", fs), "Added filesystem must not exist");
  }

  @Test
  void ensureRetrievingRegistryWorks() {
    val fs = mock(FileSystem.class);
    registry.add("com.whatever.bean", fs);
    assertSame(fs, registry.get("com.whatever.bean"), "registry must be retrieved");
  }

  @Test
  void ensureInsertingMultipleLevelsDeepWorks() {
    val fs = mock(FileSystem.class);
    val fs1 = mock(FileSystem.class);
    val fs2 = mock(FileSystem.class);
    registry.add("com", fs);
    registry.add("com.whatever", fs1);
    registry.add("com.whatever.bean", fs2);

    assertSame(registry.get("com"), fs, "entry must be correct");
    assertSame(registry.get("com.whatever"), fs1, "entry must be correct");
    assertSame(registry.get("com.whatever.bean"), fs2, "entry must be correct");
    assertEquals(registry.list().size(), 3, "size must be correct");
  }

  @Test
  void ensureListingEntriesWorks() {

    val fs = mock(FileSystem.class);
    registry.add("com", fs);
    assertEquals(Arrays.asList(fs), registry.list(), "listing entries must work");
  }

  @Test
  void ensureCollectingAllValuesAtALevelWorks() {
    val fs = mock(FileSystem.class);
    val fs1 = mock(FileSystem.class);
    val fs2 = mock(FileSystem.class);
    registry.add("com.a", fs1);
    registry.add("com.b", fs2);
    registry.add("com.c", fs);
    registry.add("com.whatever.c", fs);
    val level = registry.in("com");
    assertEquals(level.size(), 3, "must contain children");
    assertEquals(registry.in("com.whatever").size(), 1, "must contain child");
  }

  @Test
  void ensureListingEntriesWorksForNestedHierarchy() {
    val fs = mock(FileSystem.class);
    val fs1 = mock(FileSystem.class);
    val fs2 = mock(FileSystem.class);
    registry.add("com.a", fs1);
    registry.add("com.b", fs2);
    registry.add("com.c", fs);

    assertEquals(registry.root.children.size(), 1, "root child count must be 1");
    assertEquals(
        registry.root.children.get(0).children.size(), 3, "root must have 3 grandchildren");

    assertEquals(
        registry.list(), Arrays.asList(fs, fs2, fs1), "registry must have the correct entries");
  }
}
