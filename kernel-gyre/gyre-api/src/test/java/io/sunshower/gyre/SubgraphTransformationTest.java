package io.sunshower.gyre;

import static io.sunshower.gyre.DirectedGraph.outgoing;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubgraphTransformationTest {
  private Graph<DirectedGraph.Edge<String>, String> graph;

  private Transformation<
          DirectedGraph.Edge<String>, String, Graph<DirectedGraph.Edge<String>, String>>
      transformation;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
  }

  @Test
  void ensureComplexSubgraphContainsExpectedValues() {

    graph.connect("a", "b", outgoing("a -> b"));
    graph.connect("b", "c", outgoing("b -> c"));
    graph.connect("b", "d", outgoing("b -> d"));
    graph.connect("c", "e", outgoing("c -> e"));
    graph.connect("d", "f", outgoing("d -> f"));
    graph.connect("e", "f", outgoing("e -> f"));

    transformation = new SubgraphTransformation<>("c");
    val result = transformation.apply(graph);
    assertEquals(result.size(), 3);
    assertTrue(result.containsEdge("c", "e"));
    assertTrue(result.containsEdge("e", "f"));

    assertTrue(result.containsVertex("c"));
    assertTrue(result.containsVertex("e"));
    assertTrue(result.containsVertex("f"));
  }

  @Test
  void ensureTrivialSubgraphContainsExpectedValues() {
    graph.add("a");

    transformation = new SubgraphTransformation<>("a");
    val result = transformation.apply(graph);
    assertEquals(result.size(), 1);
    assertTrue(result.containsVertex("a"));
  }

  @Test
  void ensureSimpleSubgraphContainsExpectedValues() {
    graph.connect("a", "b", outgoing("a -> b"));
    transformation = new SubgraphTransformation<>("a");
    val result = transformation.apply(graph);
    assertEquals(result.size(), 2);
    assertTrue(result.neighbors("a").contains("b"));
  }

  @Test
  void ensureSimpleTrueSubgraphContainsExpectedValues() {
    graph.connect("a", "b", outgoing("a -> b"));
    graph.connect("b", "c", outgoing("b -> c"));

    transformation = new SubgraphTransformation<>("b");
    val result = transformation.apply(graph);
    assertEquals(result.size(), 2);
    assertFalse(result.containsVertex("a"));
    assertTrue(result.containsVertex("b"));
    assertTrue(result.containsVertex("c"));
    assertTrue(result.containsEdge("b", "c"));
  }
}
