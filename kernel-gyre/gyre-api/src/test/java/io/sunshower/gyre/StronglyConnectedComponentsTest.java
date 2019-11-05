package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.sunshower.gyre.DirectedGraph.outgoing;
import static org.junit.jupiter.api.Assertions.*;

class StronglyConnectedComponentsTest {

  private AbstractDirectedGraph<String, String> graph;
  private StronglyConnectedComponents<DirectedGraph.Edge<String>, String> cycleDectector;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
    cycleDectector = new StronglyConnectedComponents<>();
  }

  @Test
  void ensureDetectingComponentsWorksForSimpleCycle() {
    graph.connect("a", "b", outgoing("a -> b"));
    graph.connect("b", "a", outgoing("b -> a"));
    val g = cycleDectector.apply(graph);
    assertTrue(g.isCyclic());

    val cycles = g.getElements(Component::isCyclic);
    assertEquals(cycles.get(0).getOrigin(), Pair.of(null, "a"));
  }

  @Test
  void ensureSimpleCycleStructureIsCorrect() {

    graph.connect("a", "b", outgoing("a -> b"));
    graph.connect("b", "a", outgoing("b -> a"));
    val g = cycleDectector.apply(graph);
    assertTrue(g.isCyclic());

    val cycles = g.getElements(Component::isCyclic);
    val cycle = cycles.get(0);
    assertEquals(cycle.getOrigin(), Pair.of(null, "a"));
    val eles = cycle.getElements();
    assertEquals(eles.get(0).fst, outgoing("a -> b"), "must be correct edge");
  }

  @Test
  void ensureComplexCycleIsDetected() {
    String alphabet = "abcdefghijklmnopqrstuvwxyz";

    for (int i = 0; i < alphabet.length(); i++) {
      graph.add("" + alphabet.charAt(i));
    }

    String vowels = "aeiouy";

    for (int i = 0; i < vowels.length() - 1; i++) {
      char fst = vowels.charAt(i);
      char snd = vowels.charAt(i + 1);
      val label = String.format("%s -> %s", fst, snd);
      graph.connect("" + fst, "" + snd, outgoing(label));
    }

    var c = cycleDectector.apply(graph);
    assertFalse(c.isCyclic());

    graph.connect("y", "a", outgoing("y -> a"));
    c = cycleDectector.apply(graph);
    assertTrue(c.isCyclic());
  }
}
