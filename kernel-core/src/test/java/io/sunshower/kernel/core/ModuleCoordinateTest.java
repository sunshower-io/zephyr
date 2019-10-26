package io.sunshower.kernel.core;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Coordinate;
import java.util.HashSet;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class ModuleCoordinateTest {

  @Test
  void ensureModuleCoordinateEqualityWorksForEqualCoordinates() {
    val c = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    val v = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    assertEquals(c, v, "must be equal");
  }

  @Test
  void ensureModuleCoordinatesAreHashable() {
    val hashMap = new HashSet<Coordinate>();
    val c = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    val v = ModuleCoordinate.create("io.sunshower", "test-plugin-1", "1.0.0-SNAPSHOT");
    hashMap.add(c);
    assertTrue(
        hashMap.contains(v),
        "hashmap must contain equivalent but referentially different coordinate");
  }
}
