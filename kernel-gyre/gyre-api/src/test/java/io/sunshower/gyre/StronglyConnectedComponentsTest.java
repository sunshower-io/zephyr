package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.sunshower.gyre.DirectedGraph.outgoing;
import static java.lang.String.format;
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
    assertEquals(eles.get(1).fst, outgoing("a -> b"), "must be correct edge");
  }

  @Test
  void ensureComplexMultiComponentsAreDetected() {

    // first
    c("1", "2");

    //second
    c("2", "4");
    c("2", "5");
    c("4", "5");
    c("5", "2");


    //third
    c("2", "3");
    c("3", "6");
    c("6", "3");
    c("5", "6");


    //fourth
    c("4", "7");
    c("5", "7");
    c("6", "8");
    c("7", "8");
    c("8", "7");
    c("9", "7");
    c("7", "10");
    c("10", "9");
    c("10", "11");
    c("11", "12");
    c("12", "10");



    val c = cycleDectector.apply(graph);
    assertEquals(c.getElements().size(), 4);


  }


  @Test
  void ensureTopologicalOrderIsCorrect() {
    graph.connect("5", "11", outgoing("5 -> 11"));
    graph.connect("11", "2", outgoing("11 -> 2"));
    graph.connect("7", "11", outgoing("7 -> 11"));
    graph.connect("7", "8", outgoing("7 -> 8"));
    graph.connect("3", "8", outgoing("3 -> 8"));
    graph.connect("3", "10", outgoing("3 -> 10"));
    graph.connect("11", "9", outgoing("11 -> 9"));
    graph.connect("8", "9", outgoing("8 -> 9"));
    graph.connect("11", "10", outgoing("11 -> 10"));
    val c = cycleDectector.apply(graph);
    System.out.println(c);
  }

  @Test
  void ensureScenario1IsCorrect() {
    c("1", "0");
    c("0", "2");
    c("2", "1");
    c("0", "3");
    c("3", "4");
    val g = cycleDectector.apply(graph);

    val els = g.getElements();
    assertEquals(els.size(), 3, "must have 3 elements");
    var el = els.get(0);
    assertEquals(el.getElements().get(0).snd, "4");
    el = els.get(1);
    assertEquals(el.getElements().size(), 1);
    assertEquals(el.getElements().get(0).snd, "3");
    el = els.get(2);
    assertEquals(el.getElements().size(), 3);
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
      val label = format("%s -> %s", fst, snd);
      graph.connect("" + fst, "" + snd, outgoing(label));
    }

    var c = cycleDectector.apply(graph);
    assertFalse(c.isCyclic());

    graph.connect("y", "a", outgoing("y -> a"));
    c = cycleDectector.apply(graph);
    assertTrue(c.isCyclic());
  }

  private void c(String source, String target) {
    graph.connect(source, target, outgoing(format("%s -> %s", source, target)));
  }
}
