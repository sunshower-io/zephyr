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
  void ensureDisconnectingAdjacenciesWorks() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(values.containsEdge(source, target), "connected edge must exist");
    values.disconnect(source, target, edge);
    assertFalse(values.containsEdge(source, target), "disconnected edge must not exist");
  }

  @Test
  void ensureRetrievingEdgesIsCorrect() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(values.getEdges().contains(edge), "edge must exist in edge set");
  }

  @Test
  void ensureDirectionallyAdjacentEdgesProducesCorrectResults() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(
        values.adjacentEdges(source, DirectedGraph.Direction.Incoming).contains(edgeLabel),
        "edge must exist");
  }

  @Test
  void ensureNeighborVerticesAreCorrect() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(values.neighbors(source).contains(target), "edge must exist");
  }

  @Test
  void ensureAdjacentEdgesProducesCorrectResults() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(values.adjacentEdges(source).contains(edge), "edge must exist");
  }

  @Test
  void ensureDirectionalContainsEdgeProducesCorrectResults() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertTrue(
        values.containsEdge(source, target, DirectedGraph.Direction.Incoming),
        "directionally connected edge must exist");
    assertFalse(
        values.containsEdge(source, target, DirectedGraph.Direction.Outgoing),
        "invalidly directional edge must not exist");
  }

  @Test
  void ensureEdgeCountWorks() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    var edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Outgoing);
    values.connect(source, target, edge);
    assertEquals(values.edgeCount(), 2, "edge count must be correct");
  }

  @Test
  void ensureDirectionalDegreeOfWorks() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    var edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    edge = new DirectedGraph.Edge<>(new Object(), DirectedGraph.Direction.Outgoing);
    values.connect(source, target, edge);
    assertEquals(
        values.degreeOf(source, DirectedGraph.Direction.Outgoing), 1, "degree of must be correct");
    assertEquals(
        values.degreeOf(source, DirectedGraph.Direction.Incoming), 1, "degree of must be correct");
  }

  @Test
  void ensureDegreeOfWorks() {

    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    var edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    edge = new DirectedGraph.Edge<>(new Object(), DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);
    assertEquals(values.degreeOf(source), 2, "degree of must be correct");
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
  void ensureRetrievingSourceWorks() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);

    assertEquals(source, values.getSource(edge), "source must be correct");
  }

  @Test
  void ensureRetrievingTargetWorks() {
    val source = new Object();
    val target = new Object();
    val edgeLabel = new Object();
    val edge = new DirectedGraph.Edge<>(edgeLabel, DirectedGraph.Direction.Incoming);
    values.connect(source, target, edge);

    assertEquals(target, values.getTarget(edge), "target must be correct");
  }

  @Test
  void ensureGraphDoesNotContainEdgesInitially() {
    assertEquals(values.size(), 0, "graph must be empty");
    assertEquals(values.getEdges().size(), 0, "graph must be empty");
    assertEquals(values.getEdges().size(), 0, "graph must initially be empty");
  }
}
