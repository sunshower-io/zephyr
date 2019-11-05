package io.sunshower.gyre;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransitiveReductionTest {

  private DirectedGraph<String, String> graph;
  private TransitiveReduction<DirectedGraph.Edge<String>, String> reduction;

  @BeforeEach
  void setUp() {
    graph = new AbstractDirectedGraph<>();
    graph.connect("a", "b", DirectedGraph.incoming("ab"));
    graph.connect("a", "c", DirectedGraph.incoming("ac"));
    graph.connect("a", "e", DirectedGraph.incoming("ae"));
    graph.connect("b", "d", DirectedGraph.incoming("bd"));
    graph.connect("c", "d", DirectedGraph.incoming("cd"));
    graph.connect("c", "e", DirectedGraph.incoming("ce"));
    graph.connect("d", "e", DirectedGraph.incoming("de"));
    reduction = new TransitiveReduction<>();
  }

  @Test
  void ensureTransitiveReductionProducesCorrectEdgeCount() {
    val result =
        reduction.apply(graph, EdgeFilters.directionFilter(DirectedGraph.Direction.Incoming));
    assertEquals(result.edgeCount(), 5);
  }

  @Test
  void ensureTransitiveReductionRemovesRedudantEdges() {
    val result = reduction.apply(graph);
    assertFalse(result.containsEdge("a", "e"), "edge between a and e must be removed");
    assertTrue(graph.containsEdge("a", "e"), "edge between a and e must be preserved in original");
  }
}
