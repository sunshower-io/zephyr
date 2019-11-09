package io.sunshower.kernel.dependencies;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

import io.sunshower.gyre.Component;
import io.sunshower.gyre.DirectedGraph;
import io.sunshower.gyre.Partition;
import io.sunshower.kernel.Coordinate;
import io.sunshower.kernel.Dependency;
import io.sunshower.kernel.Module;
import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.test.MockModule;
import java.util.*;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ModuleCycleDetectorTest {

  private List<Module> graph;

  MockModule a = create("a", "a", "1.0.0");
  MockModule b = create("b", "b", "1.0.0");
  MockModule c = create("c", "c", "1.0.0");
  MockModule d = create("d", "c", "1.0.0");
  MockModule e = create("e", "c", "1.0.0");
  MockModule f = create("f", "c", "1.0.0");
  MockModule g = create("g", "c", "1.0.0");
  MockModule h = create("h", "c", "1.0.0");
  MockModule i = create("i", "c", "1.0.0");
  MockModule j = create("j", "c", "1.0.0");
  MockModule k = create("k", "c", "1.0.0");
  MockModule l = create("l", "c", "1.0.0");
  MockModule m = create("m", "c", "1.0.0");
  MockModule n = create("n", "c", "1.0.0");
  MockModule o = create("o", "c", "1.0.0");
  MockModule p = create("p", "c", "1.0.0");
  MockModule q = create("q", "c", "1.0.0");

  @BeforeEach
  void setUp() {

    graph = Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q);
  }
  /**
   * Since a module already contains its own dependencies, it's connected by the trivial path. It
   * may be a <i>validation</i> issue to detect self-cycles, but it isn't really a problem from a
   * cyclic dependency loading perspective
   */
  @Test
  void ensureSelfCycleDoesNotAppear() {
    val module = create("test", "test", "1.0.0");
    module.addDependency(new Dependency(Dependency.Type.Service, module.getCoordinate()));

    val components = newDetector(Collections.singletonList(module));
    assertEquals(getCycles(components).size(), 0, "must be allowed");
  }

  @Test
  void ensureNoCyclesAreDetectedForTrivialGraph() {
    val module = create("test", "test", "1.0.0");
    val components = newDetector(Collections.singletonList(module));
    assertEquals(getCycles(components).size(), 0, "no cycle must be detected");
  }

  @Test
  void ensureNonCyclicSimpleDependencyIsAllowed() {
    val a = create("a", "a", "1.0.3");
    val b = create("b", "b", "1.0.3");
    connect(a, b);
    val result = getCycles(newDetector(asList(a, b)));
    assertTrue(result.isEmpty(), "no cycles were detected");
  }

  private void connect(MockModule a, MockModule b) {
    a.addDependency(new Dependency(Dependency.Type.Service, b.getCoordinate()));
  }

  @Test
  void ensureCycleDetectorDetectsSimpleCycle() {
    val a = create("a", "a", "1.0.3");
    val b = create("b", "b", "1.0.3");

    a.addDependency(new Dependency(Dependency.Type.Service, b.getCoordinate()));
    b.addDependency(new Dependency(Dependency.Type.Service, a.getCoordinate()));
    val result = newDetector(asList(a, b));
    assertEquals(getCycles(result).size(), 1);
    val cycle = getCycles(result).get(0);
    assertEquals(cycle.size(), 2);
    assertEquals(cycle.getElements().get(0).snd.getName(), "a");
    assertEquals(cycle.getElements().get(1).snd.getName(), "b");
  }

  @Test
  void ensureComplexCycleIsDetected() {

    List<Module> graph = Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q);

    /** Large cycle a -> d -> h -> L -> n -> k -> f -> a */
    connect(a, d);
    connect(d, h);
    connect(h, l);
    connect(l, n);
    connect(n, k);
    connect(k, f);
    connect(f, a);

    val cycle = getCycles(newDetector(graph)).get(0);
    assertEquals(cycle.size(), 7, "must have 7 components");
  }

  @Test
  void ensureCycleIsComputedCorrectly() {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";

    val modules = new ArrayList<Module>();
    for (int i = 0; i < alphabet.length(); i++) {
      val c = String.valueOf(alphabet.charAt(i));
      modules.add(create(c, c, "1.0.0-SNAPSHOT"));
    }

    val c = newDetector(modules);
    assertFalse(c.isCyclic(), "no cycles must exist");
  }

  private Partition<DirectedGraph.Edge<Coordinate>, Coordinate> newDetector(
      Collection<Module> modules) {
    val g = new DefaultDependencyGraph();
    g.addAll(modules);
    return g.computeCycles();
  }

  @Test
  void ensureAllModulesAreRepresented() {
    val five = create("5", "5", "1.0.0");
    val seven = create("7", "7", "1.0.0");
    val three = create("3", "3", "1.0.0");
    val eleven = create("11", "11", "1.0.0");
    val eight = create("8", "8", "1.0.0");
    val two = create("2", "2", "1.0.0");
    val nine = create("9", "9", "1.0.0");
    val ten = create("10", "10", "1.0.0");

    connect(five, eleven);
    connect(eleven, two);

    connect(seven, eleven);
    connect(seven, eight);
    connect(three, eight);
    connect(three, ten);

    connect(eleven, nine);
    connect(eight, nine);
    connect(eleven, ten);
    val c = newDetector(Arrays.asList(five, seven, three, eleven, eight, two, nine, ten));
    assertFalse(c.isCyclic());
  }

  @Test
  void ensureComputedTopologicalOrderIsCorrect() {
    connect(a, d);
    connect(d, h);
    connect(h, l);
    connect(l, n);
    connect(n, k);
    connect(k, f);

    val cycle = newDetector(graph);

    assertTrue(getCycles(cycle).isEmpty(), "no cycles must be found");
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void assertCycle(
      Component<DirectedGraph.Edge<Coordinate>, Coordinate> component, String... labels) {

    assertEquals(component.size(), labels.length, "cycle length must be equal to label length");

    val members = component.getElements();
    val sublist = members.subList(0, members.size() - 1);
    Collections.reverse(sublist);
    val iter = sublist.iterator();
    for (int i = 0; i < labels.length - 1; i++) {
      val label = labels[i];
      val g = iter.next().snd.getGroup();
      assertEquals(label, g, "must be equal");
    }
    assertEquals(
        members.get(members.size() - 1).snd.getGroup(),
        labels[labels.length - 1],
        "last element must be the same");
  }

  private MockModule create(String group, String name, String version) {
    return new MockModule(ModuleCoordinate.create(group, name, version), new ArrayList<>());
  }

  private List<io.sunshower.gyre.Component<DirectedGraph.Edge<Coordinate>, Coordinate>> getCycles(
      Partition<DirectedGraph.Edge<Coordinate>, Coordinate> components) {
    return components
        .getElements()
        .stream()
        .filter(io.sunshower.gyre.Component::isCyclic)
        .collect(Collectors.toList());
  }
}
