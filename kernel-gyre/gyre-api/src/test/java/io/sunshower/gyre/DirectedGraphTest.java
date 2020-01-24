package io.sunshower.gyre;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DirectedGraphTest {

  @Test
  void ensureIntIsInitiallyUndirected() {
    byte i = 0;
    assertFalse(
        DirectedGraph.Direction.is(i, DirectedGraph.Direction.Incoming), "must not be incomming");
    assertFalse(
        DirectedGraph.Direction.is(i, DirectedGraph.Direction.Outgoing), "must not be outgoing");
  }

  @Test
  void ensureSettingIncomingWorks() {
    byte i = 0;
    assertFalse(
        DirectedGraph.Direction.is(i, DirectedGraph.Direction.Incoming),
        "must not initally be incoming");
    i = DirectedGraph.Direction.Incoming.set(i);
    assertTrue(
        DirectedGraph.Direction.is(i, DirectedGraph.Direction.Incoming),
        "must not initally be incoming");
  }

  @Test
  void ensureSettingBothWorks() {
    byte i = 0;
    i = DirectedGraph.Direction.Incoming.set(i);
    i = DirectedGraph.Direction.Outgoing.set(i);
    assertTrue(DirectedGraph.Direction.Outgoing.is(i), "must be outgoing");
    assertTrue(DirectedGraph.Direction.Incoming.is(i), "must be incoming");
  }

  @Test
  void ensureClearingOneWorks() {
    byte i = 0;
    i = DirectedGraph.Direction.Incoming.set(i);
    i = DirectedGraph.Direction.Outgoing.set(i);
    i = DirectedGraph.Direction.Outgoing.clear(i);
    assertFalse(DirectedGraph.Direction.Outgoing.is(i), "must be cleared");
  }
}
