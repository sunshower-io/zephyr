package io.sunshower.gyre;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractDirectedGraphTest {

  DirectedGraph<Object, Object> values;

  @BeforeEach
  void setUp() {
    values = new AbstractDirectedGraph<>();
  }

  @Test
  void ensureConnectingObjectsWorks() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(values.containsEdge(source, target), "connected edge must exist");
  }

  @Test
  void ensureConnectingProducesCorrectStructureSizes() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertEquals(values.edgeCount(), 1, "must have single edge");
    assertEquals(values.vertexCount(), 2, "must have 2 vertices");
    assertEquals(values.size(), 2, "must have correct size");
  }

  @Test
  void ensureGraphDoesNotContainEdgesInitially() {
    assertEquals(values.size(), 0, "graph must be empty");
    assertEquals(values.getEdges().size(), 0, "graph must be empty");
    assertEquals(values.getEdges().size(), 0, "graph must initially be empty");
  }
}
