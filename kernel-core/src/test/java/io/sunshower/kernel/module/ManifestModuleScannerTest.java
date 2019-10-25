package io.sunshower.kernel.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.sunshower.kernel.Dependency;
import io.sunshower.kernel.core.SemanticVersion;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ManifestModuleScannerTest {

  ManifestModuleScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new ManifestModuleScanner();
  }

  @Test
  void ensureValidServiceDependencyStringIsParsed() {
    val deps = scanner.parseDependencies("service@test:whatever:1.0.0");
    assertEquals(deps.size(), 1);
  }

  @Test
  void ensureServiceDependencyHasCorrectType() {
    val deps = scanner.parseDependencies("service@test:whatever:1.0.0");
    val dep = deps.get(0);
    assertEquals(dep.getType(), Dependency.Type.Service);
  }

  @Test
  void ensureServiceDependencyHasCorrectGroup() {
    val deps = scanner.parseDependencies("service@test:whatever:1.0.0");
    val dep = deps.get(0);
    assertEquals(dep.getCoordinate().getGroup(), "test");
  }

  @Test
  void ensureServiceDependencyHasCorrectName() {
    val deps = scanner.parseDependencies("service@test:whatever:1.0.0");
    val dep = deps.get(0);
    assertEquals(dep.getCoordinate().getName(), "whatever");
  }

  @Test
  void ensureServiceDependencyHasCorrectVersion() {
    val deps = scanner.parseDependencies("service@test:whatever:1.0.0");
    val dep = deps.get(0);
    assertEquals(dep.getCoordinate().getVersion(), new SemanticVersion("1.0.0"));
  }
}
