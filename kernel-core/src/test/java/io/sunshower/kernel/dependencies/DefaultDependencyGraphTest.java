package io.sunshower.kernel.dependencies;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.Dependency;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.test.MockModule;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class DefaultDependencyGraphTest {

  private DependencyGraph graph;

  @BeforeEach
  void setUp() {
    graph = new DefaultDependencyGraph();
  }

  @Test
  void ensureAddingDependenciesWorks() {
    graph.add(module("a"));
    assertEquals(graph.size(), 1, "must have one module");
  }

  @Test
  void ensureRemovingEquivalentModuleWorks() {
    val mod1 = module("a");
    val mod2 = module("a");
    graph.add(mod1);
    graph.remove(mod2);
    assertEquals(graph.size(), 0, "must remove module");
  }

  @Test
  void ensureRetrievingByCoordinateWorks() {
    val mod1 = module("a");
    graph.add(mod1);
    val module = graph.get(mod1.getCoordinate());
    assertEquals(module, mod1, "must retrieve correct module");
  }

  @Test
  void ensureRetrievingDependenciesProducesEmptySetForModuleWithNoDependencies() {
    val mod1 = module("a");
    assertTrue(
        graph.getUnresolvedDependencies(mod1).getDependencies().isEmpty(),
        "must not have any unresolved dependencies");
  }

  @Test
  void ensureRetrievingUnresolvedDependenciesForModuleWorks() {
    val module = module("a", "b");
    val unresolved = graph.getUnresolvedDependencies(module);
    assertEquals(unresolved.getDependencies().size(), 1, "must have one unresolved");
  }

  @Test
  void ensureBulkInstallationOfSatisifiedDependenciesProducesEmptyUnresolvedDependencySets() {
    val module = module("a", "b");
    val dependency = module("b");
    val results = graph.getUnresolvedDependencies(Arrays.asList(dependency, module));
    assertTrue(
        results.stream().allMatch(t -> t.getDependencies().isEmpty()),
        "must have no unresolved dependencies");
  }

  @Test
  void ensureBulkAddAddsAllDependenciesSuccessfully() {
    val module = module("a", "b");
    val dependency = module("b");
    graph.addAll(Arrays.asList(dependency, module));
    assertEquals(graph.size(), 2, "must have correct dependency count");
  }

  @Test
  void ensureRetrievingDependentsWorks() {

    val module = module("a", "b", "c");
    val dependency = module("b");
    val dep2 = module("c", "b");
    graph.addAll(Arrays.asList(dependency, module, dep2));
    val dependants = graph.getDependents(dependency.getCoordinate());
    assertTrue(dependants.contains(module), "must have correct dependent");
    assertEquals(dependants.size(), 2, "must have correct dependent count");
  }

  Module module(String gandname, String... deps) {
    val coord = ModuleCoordinate.create(gandname, gandname, "1.0.0-SNAPSHOT");
    val results =
        Arrays.stream(deps)
            .map(t -> new Dependency(Dependency.Type.Service, module(t).getCoordinate()))
            .collect(Collectors.toList());
    return new MockModule(coord, results);
  }
}
