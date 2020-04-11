package io.zephyr.kernel.dependencies;

import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.kernel.test.MockModule;
import io.zephyr.kernel.Dependency;
import io.zephyr.kernel.Module;
import io.zephyr.kernel.core.ModuleCoordinate;
import io.zephyr.kernel.core.SemanticVersion;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
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

  @Test
  void ensureRetrievingLevelWorks() {
    firstScenario();

    val query = ModuleCoordinate.group("test").name("coolbeans");

    val modules = graph.getModules(query);
    assertEquals(modules.size(), 1, "must return 1 result");
  }

  @Test
  void ensureRetrievingLevelAtGroupLevelWorks() {
    firstScenario();
    val query = ModuleCoordinate.group("test");
    val modules = graph.getModules(query);
    assertEquals(modules.size(), 2, "must return 2 results");
  }

  @Test
  void ensureRetrievingEarliestProducesFirstVersion() {
    scenario2();
    val query = ModuleCoordinate.group("io.sunshower").name("sunshower-spring");
    val module = graph.earliest(query);
    val coord = module.getCoordinate();
    assertEquals(new SemanticVersion("1.0.0-SNAPSHOT"), coord.getVersion());
  }

  @Test
  void ensureRetrievingLatestProducesLastVersion() {
    scenario2();
    val query = ModuleCoordinate.group("io.sunshower").name("sunshower-spring");
    val module = graph.earliest(query);
    val coord = module.getCoordinate();
    assertEquals(new SemanticVersion("1.0.0-SNAPSHOT"), coord.getVersion());
  }

  private void scenario2() {
    val fst = moduleWithVersion("io.sunshower", "sunshower-spring", "1.0.0-SNAPSHOT");
    val snd = moduleWithVersion("io.sunshower", "sunshower-spring", "1.1.0-Final");
    val third = moduleWithVersion("io.sunshower", "sunshower-spring", "1.2.0-Final");
    graph.addAll(Arrays.asList(fst, snd, third));
  }

  Module moduleWithVersion(String group, String name, String version, String... deps) {
    val coord = ModuleCoordinate.create(group, name, version);
    val results =
        Arrays.stream(deps)
            .map(t -> new Dependency(Dependency.Type.Service, module(t).getCoordinate()))
            .collect(Collectors.toList());
    return new MockModule(coord, results);
  }

  Module newModule(String group, String name, String... deps) {
    val coord = ModuleCoordinate.create(group, name, "1.0.0-SNAPSHOT");
    val results =
        Arrays.stream(deps)
            .map(t -> new Dependency(Dependency.Type.Service, module(t).getCoordinate()))
            .collect(Collectors.toList());
    return new MockModule(coord, results);
  }

  private void firstScenario() {
    val fst = newModule("test", "coolbeans");
    val snd = newModule("test", "beanbeans");

    graph.addAll(Arrays.asList(fst, snd));
  }

  Module module(String gandname, String... deps) {
    return newModule(gandname, gandname, deps);
  }
}
