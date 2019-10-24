package io.sunshower.kernel;

import static io.sunshower.kernel.dependencies.ModuleCycleDetector.newDetector;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.sunshower.kernel.core.ModuleCoordinate;
import io.sunshower.kernel.dependencies.ModuleCycleDetector;
import java.util.*;
import lombok.val;
import org.junit.jupiter.api.Test;

@SuppressWarnings({
  "PMD.UnusedPrivateMethod",
  "PMD.AvoidDuplicateLiterals",
  "PMD.JUnitTestContainsTooManyAsserts",
  "PMD.JUnitAssertionsShouldIncludeMessage"
})
class ModuleCycleDetectorTest {

  /**
   * Since a module already contains its own dependencies, it's connected by the trivial path. It
   * may be a <i>validation</i> issue to detect self-cycles, but it isn't really a problem from a
   * cyclic dependency loading perspective
   */
  @Test
  void ensureSelfCycleDoesNotAppear() {
    val module = create("test", "test", "1.0.0");
    module.addDependency(module.getCoordinate());

    val components = newDetector(Collections.singletonList(module)).compute();
    assertTrue(components.isEmpty(), "must be allowed");
  }

  @Test
  void ensureNoCyclesAreDetectedForTrivialGraph() {
    val module = create("test", "test", "1.0.0");
    val components = newDetector(Collections.singletonList(module)).compute();
    assertTrue(components.isEmpty(), "no cycle must be detected");
  }

  @Test
  void ensureNonCyclicSimpleDependencyIsAllowed() {
    val a = create("a", "a", "1.0.3");
    val b = create("b", "b", "1.0.3");
    connect(a, b);
    val result = newDetector(asList(a, b)).compute();
    assertTrue(result.isEmpty(), "no cycles were detected");
  }

  private void connect(MockModule a, MockModule b) {
    a.addDependency(b.getCoordinate());
  }

  @Test
  void ensureCycleDetectorDetectsSimpleCycle() {
    val a = create("a", "a", "1.0.3");
    val b = create("b", "b", "1.0.3");

    a.addDependency(b.getCoordinate());
    b.addDependency(a.getCoordinate());
    val result = newDetector(asList(a, b)).compute();
    assertEquals(result.size(), 1);
    val cycle = result.get(0);
    assertEquals(cycle.size(), 2);
    assertEquals(cycle.getMembers().get(0).getCoordinate().getName(), "b");
    assertEquals(cycle.getMembers().get(1).getCoordinate().getName(), "a");
  }

  @Test
  void ensureComplexCycleIsDetected() {
    val a = create("a", "a", "1.0.0");
    val b = create("b", "b", "1.0.0");
    val c = create("c", "c", "1.0.0");
    val d = create("d", "c", "1.0.0");
    val e = create("e", "c", "1.0.0");
    val f = create("f", "c", "1.0.0");
    val g = create("g", "c", "1.0.0");
    val h = create("h", "c", "1.0.0");
    val i = create("i", "c", "1.0.0");
    val j = create("j", "c", "1.0.0");
    val k = create("k", "c", "1.0.0");
    val l = create("l", "c", "1.0.0");
    val m = create("m", "c", "1.0.0");
    val n = create("n", "c", "1.0.0");
    val o = create("o", "c", "1.0.0");
    val p = create("p", "c", "1.0.0");
    val q = create("q", "c", "1.0.0");

    List<Module> graph = Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q);

    /** Large cycle a -> d -> h -> L -> n -> k -> f -> a */
    connect(a, d);
    connect(d, h);
    connect(h, l);
    connect(l, n);
    connect(n, k);
    connect(k, f);
    connect(f, a);

    val cycle = newDetector(graph).compute().get(0);
    assertCycle(cycle, "d", "h", "l", "n", "k", "f", "a");
  }

  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  private void assertCycle(ModuleCycleDetector.Component component, String... labels) {

    assertEquals(component.size(), labels.length, "cycle length must be equal to label length");

    val members = component.getMembers();
    val sublist = members.subList(0, members.size() - 1);
    Collections.reverse(sublist);
    val iter = sublist.iterator();
    for (int i = 0; i < labels.length - 1; i++) {
      val label = labels[i];
      val g = iter.next().getCoordinate().getGroup();
      assertEquals(label, g, "must be equal");
    }
    assertEquals(
        members.get(members.size() - 1).getCoordinate().getGroup(),
        labels[labels.length - 1],
        "last element must be the same");
  }

  private MockModule create(String group, String name, String version) {
    return new MockModule(ModuleCoordinate.create(group, name, version), new ArrayList<>());
  }
}
