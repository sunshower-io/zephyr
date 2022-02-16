package io.zephyr.kernel.core;

import static org.junit.jupiter.api.Assertions.*;

import io.zephyr.kernel.Coordinate;
import java.util.LinkedHashSet;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ModuleCoordinateTest {

  @Test
  void ensureModuleCoordinatesWorkForSemVer() {
    val c = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0");
    val v = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.1");
    assertEquals(c, v, "must be equal");
  }
  @Test
  void ensureModuleCoordinateEqualityWorksForEqualCoordinates() {
    val c = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    val v = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    assertEquals(c, v, "must be equal");
  }

  @Test
  void ensureModuleCoordinatesAreHashable() {
    val hashMap = new LinkedHashSet<Coordinate>();
    val c = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    val v = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    hashMap.add(c);
    assertTrue(
        hashMap.contains(v),
        "hashmap must contain equivalent but referentially different coordinate");
  }
}
